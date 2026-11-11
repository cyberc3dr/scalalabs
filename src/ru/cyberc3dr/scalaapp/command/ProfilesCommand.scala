package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, ConfigNotLoaded, Internal}
import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.model.Profile

object ProfilesParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] = Right(ProfilesCommand)

object ProfilesExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case ProfilesCommand =>
      App.config match
        case None => Left(ConfigNotLoaded)
        case Some(config) =>
          val text = config.profiles.map(formatProfile).mkString("\n")
          Right(CommandResult(text))
    case _ => Left(Internal("ProfilesExecutor получил чужую команду"))

  private def formatProfile(name: String, profile: Profile): String =
    s"""Профиль (id: $name)"
       |${"=".repeat(40)}
       |Описание: ${profile.description}
       |Шлюз: ${profile.gateway}
       |Пингов: ${profile.pingCount} | Timeout: ${profile.timeoutMilliseconds} мс
       |Максимальное кол-во хопов: ${profile.maximumRouteHops}
       |
       |Таргеты
       |  IP: ${profile.externalIpTargets.mkString(", ")}
       |  DNS: ${profile.dnsNames.mkString(", ")}
       |  HTTP: ${profile.httpTargets.mkString(", ")}
       |
       |Пороги:
       |  Задержка (пред/крит): ${profile.thresholds.warningLatencyMilliseconds} / ${profile.thresholds.criticalLatencyMilliseconds}
       |  % потери пакетов (пред/крит): ${profile.thresholds.warningPacketLossPercent} / ${profile.thresholds.criticalPacketLossPercent}
       |  Время обращения к HTTP (пред): ${profile.thresholds.warningHttpTimeMilliseconds}
       |""".stripMargin
