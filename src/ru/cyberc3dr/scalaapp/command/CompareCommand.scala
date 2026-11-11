package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, HistoryNotFound, Internal}
import ru.cyberc3dr.scalaapp.model.HistoryEntry
import ru.cyberc3dr.scalaapp.net.{CurlResult, DnsStats, PingStats, TraceStats}
import ru.cyberc3dr.scalaapp.utils.HistoryManager

import scala.util.chaining.scalaUtilChainingOps

object CompareParser extends CommandParser:
  // А тут for comprehension
  // Вот это вместо
  /*
  args.getInt.flatMap { firstId =>
    args.getInt.map { secondId =>
      CompareCommand(firstId, secondId)
    }
  }
   */
  // все для людей
  def parse(args: StrBuffer): Either[AppError, Command] =
    for
      firstId  <- args.getInt
      secondId <- args.getInt
    yield CompareCommand(firstId, secondId)

object CompareExecutor extends CommandExecutor:

  private def loss(p: Option[PingStats]): String = p match
    case Some(value) => value.lossPercent.toString + "%"
    case None => "-"

  private def lat(p: Option[PingStats]): String = p match
    case Some(value) => f"${value.avgLatency}%.1f мс"
    case None => "-"

  private def hops(t: Option[TraceStats]): String = t match
    case Some(value) => value.hops.size.toString
    case None => "-"

  private def dnsOk(ds: List[DnsStats]): String = if ds.nonEmpty && ds.exists(_.success) then "успех" else "провал"

  private def httpOk(hs: List[CurlResult]): String = if hs.nonEmpty && hs.exists(_.isAvailable) then "успех" else "провал"

  private def avgLoss(ps: List[PingStats]): Double = ps.map(_.lossPercent).pipe(it => it.sum / it.size).toInt

  private def avgLat(ps: List[PingStats]): Double = ps.map(_.avgLatency).pipe(it => it.sum / it.size).toInt

  private def buildComparison(a: HistoryEntry, b: HistoryEntry): String =
    val r1 = a.result
    val r2 = b.result

    val sb = StringBuilder()

    sb.append(f"Сравнение диагностик ${a.id}%d и ${b.id}%d (${a.profile} vs ${b.profile}):\n")
    sb.append(f"  Потери до шлюза           ${loss(r1.gatewayPing)}%-14s ${loss(r2.gatewayPing)}\n")
    sb.append(f"  Потери до внешнего узла   ${loss(r1.pings.headOption)}%-14s ${loss(r2.pings.headOption)}\n")
    sb.append(f"  Средняя задержка          ${lat(r1.pings.headOption)}%-14s ${lat(r2.pings.headOption)}\n")
    sb.append(f"  DNS                       ${dnsOk(r1.dns)}%-14s ${dnsOk(r2.dns)}\n")
    sb.append(f"  HTTP                      ${httpOk(r1.https)}%-14s ${httpOk(r2.https)}\n")
    sb.append(f"  Переходов (traceroute)    ${hops(r1.trace)}%-14s ${hops(r2.trace)}\n")

    val aLoss = avgLoss(r1.pings)
    val aLat = avgLat(r1.pings)
    val bLoss = avgLoss(r2.pings)
    val bLat = avgLat(r2.pings)

    val worseLoss = bLoss > aLoss + 5
    val worseLat = bLat > aLat + 20 && aLat > 0

    val summary = (r1.state, r2.state) match
      case (sa, sb) if sa == sb && !worseLoss && !worseLat => "Состояние в целом стабильно, существенных изменений нет."
      case (_, sb) if worseLoss || worseLat                 => s"Качество внешнего соединения ухудшилось (состояние: ${sb.name})."
      case (sa, _)                                          => s"Качество внешнего соединения улучшилось (состояние: ${sa.name})."

    sb.append(s"\nВывод: $summary")
    sb.toString

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case CompareCommand(firstId, secondId) =>
      for
        a <- HistoryManager.getById(firstId).toRight(HistoryNotFound(firstId))
        b <- HistoryManager.getById(secondId).toRight(HistoryNotFound(secondId))
      yield CommandResult(buildComparison(a, b), appendToReport = true)
    case _ => Left(Internal("CompareExecutor получил чужую команду"))
