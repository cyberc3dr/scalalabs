package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App

object CommandProfiles extends Command {
  override val name: String = "profiles"

  override def execute(ctx: CommandContext): Unit =
    // App.config ?: throw
    // с lateinit оно вообще notnull будет
    // UPD: мне нравится такой синтаксис, пойдёт
    val config = App.config match
      case Some(value) => value
      case None => throw IllegalStateException("Конфигурация не загружена по какой то причине??")

    val builder = StringBuilder()

    config.profiles.foreach { case (str, profile) =>
      // нету profile.run { // this }
      // нету with(profile) { // this }
      // а собсна почему так? ну ладно
      builder.append(
        s"""Профиль (id: $str)"
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
           |""".stripMargin)
    }

    println(builder.toString())
}
