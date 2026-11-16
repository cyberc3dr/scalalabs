package ru.cyberc3dr.scalaapp.utils

import ru.cyberc3dr.scalaapp.errors.{ExecutionError, PlatformNotSupported, UnknownUtility, UtilityUnavailable}
import ru.cyberc3dr.scalaapp.ui.UI

import scala.sys.process.{ProcessLogger, stringToProcess}

/** Результат проверки одной системной утилиты */
case class UtilityCheck(name: String, available: Boolean)

/** Отчёт о проверке окружения. */
case class EnvironmentReport(checks: List[UtilityCheck]):
  def available: List[UtilityCheck] = checks.filter(_.available)
  def missing: List[UtilityCheck] = checks.filterNot(_.available)

object EnvironmentChecker:

  private val cmds = List("ifconfig", "route", "ping", "nslookup", "dig", "traceroute", "curl")

  private var report: Option[EnvironmentReport] = None

  /** Полная проверка */
  def check(ui: UI): Either[ExecutionError, EnvironmentReport] =
    val os = System.getProperty("os.name").toLowerCase
    if !os.contains("mac") then Left(PlatformNotSupported(os))
    else
      val checks = cmds.map { cmd =>
        val isAvailable = s"which $cmd".!(ProcessLogger(_ => ())) == 0
        UtilityCheck(cmd, isAvailable)
      }
      val value = EnvironmentReport(checks)
      report = Some(value)
      Right(value)

  def printReport(value: EnvironmentReport, ui: UI): Unit =
    ui.writeLine("Проверка системных утилит:\n")
    value.checks.foreach { check =>
      val status = if check.available then "[+]" else "[-]"
      ui.writeLine(s"$status ${check.name}")
    }
    ui.writeLine("")
    value.missing.foreach { check =>
      ui.writeLine(
        s"Проверка ${checkName(check.name)} будет недоступна:\nутилита ${check.name} не найдена.\n"
      )
    }

  private def checkName(cmd: String): String = cmd match
    case "traceroute" => "маршрута"
    case "dig" | "nslookup" => "DNS"
    case "curl" => "HTTP"
    case "ping" => "доступности"
    case "ifconfig" | "route" => "сетевых интерфейсов"
    case _ => cmd

  def isAvailable(cmd: String): Either[ExecutionError, Boolean] =
    report match
      case None => Left(UnknownUtility(cmd))
      case Some(value) =>
        value.checks.find(_.name == cmd) match
          case Some(check) => Right(check.available)
          case None => Left(UnknownUtility(cmd))

  def requireAvailable(cmd: String): Either[ExecutionError, Unit] =
    isAvailable(cmd).flatMap {
      case true => Right(())
      case false => Left(UtilityUnavailable(cmd))
    }
