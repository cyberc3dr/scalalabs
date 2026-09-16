package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.model.Profile
import ru.cyberc3dr.scalaapp.net.DiagnosticState.{GatewayUnavailable, Good, NoInterface, NoInternet, ResourceUnavailable, Unknown}
import ru.cyberc3dr.scalaapp.utils.EnvironmentChecker

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.chaining.scalaUtilChainingOps

enum DiagnosticState:
  case NoInterface
  case GatewayUnavailable
  case NoInternet
  case DnsIssue
  case ResourceUnavailable
  case Unknown
  case Unstable
  case VeryUnstable
  case Good

case class DiagnosticResult(
  state: DiagnosticState,
  time: Instant = Instant.now(),
  gateway: Option[String] = None,
  gatewayPing: Option[PingStats] = None,
  pings: List[PingStats] = List.empty,
  dns: List[DnsStats] = List.empty,
  https: List[CurlResult] = List.empty,
  trace: Option[TraceStats] = None
)

def diag(profile: Profile): DiagnosticResult =
  // попробуем найти кто здесь gateway
  val gateway = Try(getGateway) match
    case Success(value) => value
    // хмм может нам повезло и дали в конфиге гетевей
    case Failure(e) => if profile.gateway != "auto" then profile.gateway else return DiagnosticResult(NoInterface)

  val pingGateway = Try(ping(gateway, profile.pingCount)) match
    case Success(value) => value
    case Failure(e) => return DiagnosticResult(GatewayUnavailable, gateway = Some(gateway))

  if pingGateway.lossPercent > 0 then return DiagnosticResult(GatewayUnavailable, gateway = Some(gateway), gatewayPing = Some(pingGateway))

  // короче класс, gateway доступен проверяем инет

  val pings = profile.externalIpTargets
    .map { ip =>
      Try(ping(ip, profile.pingCount)) match
        case Success(value) => Some(value)
        case Failure(e) => None
    }

  // наверное следует делать вывод об отсутствии интернета по комбинации диагностик
  // скипаем это все дело если команды пинг нет
  val pingsFailed = pings.flatten.forall(_.lossPercent == 100) && EnvironmentChecker.isAvailable("ping")

  val dnsChecks = profile.dnsNames
    .map { name =>
      // Будем читать что NXDOMAIN не возможен в конфигурации
      // И юзер ввел на 100% существующие домены
      // Anyway NXDOMAIN означает что DNS сервер ответил
      val future = Future {
        dig(name)
      }

      Try(Await.result(future, profile.timeoutMilliseconds.millis)) match
        case Success(value) => Some(value)
        case Failure(e) => None
    }

  val dnsFailed = dnsChecks.forall(_.isEmpty) && EnvironmentChecker.isAvailable("dig")

  val httpChecks = profile.httpTargets
    .map { address =>
      Try(curl(address)) match
        case Success(value) => Some(value)
        case Failure(e) => None
    }

  val curlFailed = httpChecks.flatten.forall(!_.isAvailable) && EnvironmentChecker.isAvailable("curl")

  if dnsFailed then
    var state = DiagnosticState.DnsIssue

    if pingsFailed && curlFailed then state = NoInternet

    // короче все пошло не по плану да да
    // хз как это тестить - у меня нет тестового оборудования

    return DiagnosticResult(
      state = NoInternet,
      gateway = Some(gateway),
      gatewayPing = Some(pingGateway),
      pings = pings.flatten,
      dns = dnsChecks.flatten,
      https = httpChecks.flatten
    )

  // нука давай traceroute попробуем

  val tracer = Try(traceroute(profile.traceTarget, profile.maximumRouteHops)) match
    case Success(value) => Some(value)
    case Failure(e) => None

  // обязательное условие успеха - traceroute должен выполниться при наличии
  val traceFailed = !tracer.exists(_.success) && EnvironmentChecker.isAvailable("traceroute")

  // let = pipe
  // also = tap
  //                         httpChecks.filterNotNull().let { it.count { !it.isAvailable } in 1 until it.length }
  //                         ну типа
  //                         спасибо что until сделали
  //                         постфиксы тоже класс, in не нашел, нету видимо
  val resourceIssuePresent = httpChecks.flatten.pipe(it => 1 until it.length contains it.count(_.isAvailable))
  // если хотя бы один ответил - значит проблема в остальных

  // говнокод начинается здесь

  // короче надо еще проверить на странности
  val strangeThingsAppeared = pings.exists {
    case Some(ping) => ping.lossPercent == 100
    case None => true
  }

  var status = if resourceIssuePresent then
    ResourceUnavailable
  else if strangeThingsAppeared then
    Unknown
  else Good

  if resourceIssuePresent || strangeThingsAppeared then return DiagnosticResult(
    state = status,
    gateway = Some(gateway),
    gatewayPing = Some(pingGateway),
    pings = pings.flatten,
    dns = dnsChecks.flatten,
    https = httpChecks.flatten,
    trace = tracer
  )

  // map и pipe будут затратны поэтому вернул сверху

  val avgLoss = pings.flatten.map(_.lossPercent).pipe(it => it.sum / it.size).toInt
  val avgLatency = pings.flatten.map(_.avgLatency).pipe(it => it.sum / it.size).toInt
  val avgHttpTime = httpChecks.flatten.map(_.timeTotalMs).pipe(it => it.sum / it.size).toInt

  val thresholds = profile.thresholds

  status = (avgLoss, avgLatency, avgHttpTime) match
    case (loss, latency, _) if loss >= thresholds.criticalPacketLossPercent ||
                               latency >= thresholds.criticalLatencyMilliseconds
      => DiagnosticState.VeryUnstable

    case (loss, latency, http) if loss >= thresholds.warningPacketLossPercent ||
                                  latency >= thresholds.warningLatencyMilliseconds ||
                                  http >= thresholds.warningHttpTimeMilliseconds
      => DiagnosticState.Unstable

    case _ => DiagnosticState.Good

  DiagnosticResult(
    state = status,
    gateway = Some(gateway),
    gatewayPing = Some(pingGateway),
    pings = pings.flatten,
    dns = dnsChecks.flatten,
    https = httpChecks.flatten,
    trace = tracer
  )