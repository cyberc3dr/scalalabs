package ru.cyberc3dr.scalaapp.net

import com.fasterxml.jackson.annotation.JsonValue
import ru.cyberc3dr.scalaapp.model.Profile
import ru.cyberc3dr.scalaapp.net.DiagnosticState.{GatewayUnavailable, Good, NoInterface, NoInternet, ResourceUnavailable, Unknown}
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, Logging}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.chaining.scalaUtilChainingOps

enum DiagnosticState(@JsonValue val name: String):
  case NoInterface extends DiagnosticState("Сетевой интерфейс не настроен")
  case GatewayUnavailable extends DiagnosticState("Недоступен локальный шлюз")
  case NoInternet extends DiagnosticState("Нет доступа в Интернет")
  case DnsIssue extends DiagnosticState("Есть проблема с DNS")
  case ResourceUnavailable extends DiagnosticState("Возможна проблема конкретного сайта")
  case Unknown extends DiagnosticState("Однозначно определить проблему не удалось")
  case Unstable extends DiagnosticState("Соединение нестабильно")
  case VeryUnstable extends DiagnosticState("Соединение очень нестабильно")
  case Good extends DiagnosticState("Всё работает штатно")

case class DiagnosticResult(
  state: DiagnosticState,
  gateway: Option[String] = None,
  gatewayPing: Option[PingStats] = None,
  pings: List[PingStats] = List.empty,
  dns: List[DnsStats] = List.empty,
  https: List[CurlResult] = List.empty,
  trace: Option[TraceStats] = None
)

def diag(profile: Profile): DiagnosticResult =
  Logging.info(s"Начата диагностика: profile=${profile.description}")

  // попробуем найти кто здесь gateway
  Logging.debug("Пытаемся найти gateway...")
  val gateway = Try(getGateway) match
    case Success(value) => value
    // хмм может нам повезло и дали в конфиге гетевей
    case Failure(e) => if profile.gateway != "auto" then profile.gateway else return DiagnosticResult(NoInterface)

  Logging.debug(s"Gateway найден: $gateway. Пробуем пинг...")

  val pingGateway = Try(ping(gateway, profile.pingCount)) match
    case Success(value) => value
    case Failure(e) => return DiagnosticResult(GatewayUnavailable, gateway = Some(gateway))

  Logging.debug("Пинг до gateway прошел.")

  if pingGateway.lossPercent > 0 then return DiagnosticResult(GatewayUnavailable, gateway = Some(gateway), gatewayPing = Some(pingGateway))

  Logging.info("Проверка завершена: type=gateway, result=success")
  Logging.debug("Потерь 0%")

  // короче класс, gateway доступен проверяем инет

  Logging.debug("Проверяем таргеты по пингу...")

  val pings = profile.externalIpTargets
    .map { ip =>
      Try(ping(ip, profile.pingCount)) match
        case Success(value) => Some(value)
        case Failure(e) => None
    }

  // наверное следует делать вывод об отсутствии интернета по комбинации диагностик
  // скипаем это все дело если команды пинг нет
  val pingsFailed = pings.flatten.forall(_.lossPercent == 100) && EnvironmentChecker.isAvailable("ping")

  pings.flatten.foreach { p =>
    if p.lossPercent > 0 then
      Logging.warn(s"Обнаружены потери пакетов: target=${p.address}, loss=${p.lossPercent}")
  }

  if pingsFailed then Logging.debug("!!! Все пинги провалились")

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

  Logging.debug("Проверяем таргеты по DNS...")

  val dnsFailed = dnsChecks.forall(_.isEmpty) && EnvironmentChecker.isAvailable("dig")

  if dnsFailed then Logging.debug("!!! Все DNS провалились")

  Logging.info("DNS-проверка завершена: result=success")

  Logging.debug("Проверяем таргеты по HTTP...")

  val httpChecks = profile.httpTargets
    .map { address =>
      Try(curl(address)) match
        case Success(value) => Some(value)
        case Failure(e) => None
    }

  val curlFailed = httpChecks.flatten.forall(!_.isAvailable) && EnvironmentChecker.isAvailable("curl")

  if curlFailed then Logging.debug("!!! Все HTTP не ответили")

  if dnsFailed then
    var state = DiagnosticState.DnsIssue

    if pingsFailed && curlFailed then state = NoInternet

    // короче все пошло не по плану да да
    // хз как это тестить - у меня нет тестового оборудования

    Logging.debug("Диагностика упала, DNS или интернета нет")

    return DiagnosticResult(
      state = NoInternet,
      gateway = Some(gateway),
      gatewayPing = Some(pingGateway),
      pings = pings.flatten,
      dns = dnsChecks.flatten,
      https = httpChecks.flatten
    )

  // нука давай traceroute попробуем

  Logging.debug("Пробуем traceroute...")

  val tracer = Try(traceroute(profile.traceTarget, profile.maximumRouteHops)) match
    case Success(value) => Some(value)
    case Failure(e) => None

  // обязательное условие успеха - traceroute должен выполниться при наличии
  val traceFailed = !tracer.exists(_.success) && EnvironmentChecker.isAvailable("traceroute")

  if traceFailed then Logging.debug("!!! Trace не был успешным")

  // let = pipe
  // also = tap
  //                         httpChecks.filterNotNull().let { it.count { !it.isAvailable } in 1 until it.length }
  //                         ну типа
  //                         спасибо что until сделали
  //                         постфиксы тоже класс, in не нашел, нету видимо
  val resourceIssuePresent = httpChecks.flatten.pipe(it => 1 until it.length contains it.count(_.isAvailable))

  if resourceIssuePresent then Logging.debug("!!! Какие то из ресурсов не ответили")
  // если хотя бы один ответил - значит проблема в остальных

  // говнокод начинается здесь

  // короче надо еще проверить на странности
  val strangeThingsAppeared = pings.exists {
    case Some(ping) => ping.lossPercent == 100
    case None => true
  }

  if resourceIssuePresent then Logging.debug("!!! Не все пинги прошли успешно")

  var status = if resourceIssuePresent then
    ResourceUnavailable
  else if strangeThingsAppeared then
    Unknown
  else if traceFailed then
    Unknown
  else Good

  if resourceIssuePresent || strangeThingsAppeared || traceFailed then return DiagnosticResult(
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

  Logging.info(s"Диагностика завершена: conclusion=${status.name.toLowerCase.replace("ё", "е").replace(" ", "-")}")
  Logging.debug(s"Вердикт: $status")

  DiagnosticResult(
    state = status,
    gateway = Some(gateway),
    gatewayPing = Some(pingGateway),
    pings = pings.flatten,
    dns = dnsChecks.flatten,
    https = httpChecks.flatten,
    trace = tracer
  )