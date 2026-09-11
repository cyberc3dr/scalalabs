package ru.cyberc3dr.scalaapp.utils

object Logging:

  def format(pingStats: PingStats): String =
    val sb = StringBuilder()

    sb.append(s"Проверка внешнего узла: ${pingStats.address}\n\n")

    sb.append(s"Отправлено пакетов: ${pingStats.packetsSent}\n")
    sb.append(s"Получено ответов: ${pingStats.packetsReceived}\n")
    sb.append(s"Потеряно: ${pingStats.lossPercent}%\n\n")

    if(pingStats.minimalLatency == -1) {
      sb.append("Состояние: ресурс не отвечает на ping\n")
      sb.append("Отсутствует соединение, либо ресурс не принимает ICMP запросы.")
      return sb.toString()
    }

    sb.append(s"Минимальная задержка: ${pingStats.minimalLatency} мс\n")
    sb.append(s"Средняя задержка: ${String.format("%.2f", pingStats.avgLatency)} мс\n")
    sb.append(s"Максимальная задержка: ${pingStats.maxLatency} мс\n\n")

    val badLoss = pingStats.lossPercent > 0
    val badLatency = pingStats.maxLatency - pingStats.minimalLatency > 20

    if(badLoss && badLatency) {
      sb.append("Состояние: нестабильное соединение\n")
      sb.append("Причина: обнаружены потери пакетов и скачки задержки")
    } else if(badLoss) {
      sb.append("Состояние: нестабильное соединение\n")
      sb.append("Причина: обнаружены потери пакетов")
    } else if(badLatency) {
      sb.append("Состояние: нестабильное соединение\n")
      sb.append("Причина: обнаружены скачки задержки")
    } else {
      sb.append("Состояние: соединение стабильно")
    }

    sb.toString()

