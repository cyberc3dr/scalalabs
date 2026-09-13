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

  def format(curl: CurlResult): String =
    val sb = StringBuilder()

    val https = curl.url.startsWith("https")

    sb.append(if https then "HTTPS: " else "HTTP: ")
    sb.append(s"${curl.url}\n\n")

    sb.append(s"Состояние: ")
    if !curl.isAvailable then
      sb.append("Недоступен")
      return sb.toString()

    sb.append("Доступен\n")
    sb.append(s"Код ответа ${curl.httpCode}\n")
    sb.append(s"Время: ${String.format("%.2f", curl.timeTotalMs)} мс\n")
    sb.append(s"Перенаправлений: ${curl.redirects}\n")
    if curl.redirects > 0 then sb.append(s"Перенаправлено на: ${curl.urlEffective}\n")
    sb.append(s"Ошибка TLS: ${if curl.hasTlsError then "есть" else "нет"}")

    sb.toString()

  def format(dns: DnsStats): String =
    val sb = StringBuilder()

    sb.append(s"DNS: ${dns.address}\n\n")
    sb.append(s"Состояние: ${if dns.success then "Успешно" else "Имя не существует"}")
    if !dns.success then return sb.toString()

    sb.append("\nПолученные адреса:\n")
    dns.addresses
      .map(s => s"- $s\n")
      .foreach(sb.append)

    sb.append(s"\nВремя выполнения: ${dns.time} мс")

    sb.toString()

  def format(trace: TraceStats): String =
    val sb = StringBuilder()

    sb.append(s"Маршрут до ${trace.address}:\n\n")

    val maxLength = trace.hops.map(_.address.length).max

    trace.hops.zipWithIndex
      .map { case (hop, i) =>
        s" ${i+1}   ${hop.address.padTo(maxLength, ' ')}     ${if hop.latency != -1 then s"${hop.latency} мс" else "нет ответа"}\n"
      }
      .foreach(sb.append)

    val success = trace.success
    sb.append(if success then "\nЦель достигнута.\n" else "\nЦель не достигнута.\n")
    sb.append(s"Переходов ${trace.hops.length}")

    sb.toString()

