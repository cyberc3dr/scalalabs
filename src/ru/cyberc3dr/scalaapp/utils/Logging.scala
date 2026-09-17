package ru.cyberc3dr.scalaapp.utils

import ru.cyberc3dr.scalaapp.net.{CurlResult, DiagnosticResult, DnsStats, PingStats, TraceStats}

object Logging:

  def debug(logs: String*): Unit = logs.foreach(it => println(s"DEBUG: $it"))

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

    val message = (badLoss, badLatency) match {
      case (true, true) => "Состояние: нестабильное соединение\nПричина: обнаружены потери пакетов и скачки задержки"
      case (true, false) => "Состояние: нестабильное соединение\nПричина: обнаружены потери пакетов"
      case (false, true) => "Состояние: нестабильное соединение\nПричина: обнаружены скачки задержки"
      case _ => "Состояние: соединение стабильно"
    }

    sb.append(message)

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

  def pingMicro(ping: PingStats): String = s"${ping.address}: ↑ ${ping.packetsSent} ↓ ${ping.packetsReceived} Loss ${ping.lossPercent}% | Min ${ping.minimalLatency} Avg ${String.format("%.2f", ping.avgLatency)} Max ${ping.maxLatency}"
  def dnsMicro(dns: DnsStats): String = s"${dns.address}: ${if dns.success then "✅" else "❌"} | ⏱️ ${dns.time} | ${dns.addresses.mkString(", ")}"
  def httpMicro(curl: CurlResult): String = s"${curl.url}: ${if curl.isAvailable then s"✅ ${curl.httpCode}" else "❌"} | ⏱️ ${String.format("%.2f", curl.timeTotalMs)} | SSL ${if curl.hasTlsError then "!!" else "OK"}${if curl.redirects > 0 then s" | => ${curl.urlEffective}" else ""}"

  def format(diag: DiagnosticResult): String =
    val sb = StringBuilder()

    sb.append(s"Время выполнения: ${diag.time.toString}\n")
    sb.append(s"Результат: ${diag.state.name}")

    diag.gateway.foreach { it =>
      sb.append(s"\n\nШлюз: $it")
      diag.gatewayPing.foreach(ping => sb.append(s"\nПинг: ${pingMicro(ping)}"))
    }

    if diag.pings.nonEmpty then
      sb.append("\n\nПинги:")
      diag.pings.foreach(it => sb.append(s"\n${pingMicro(it)}"))

    if diag.dns.nonEmpty then
      sb.append("\n\nDNS:")
      diag.dns.foreach(it => sb.append(s"\n${dnsMicro(it)}"))

    if diag.https.nonEmpty then
      sb.append("\n\nHTTP:")
      diag.https.foreach(it => sb.append(s"\n${httpMicro(it)}"))

    diag.trace.foreach { it =>
      sb.append("\n\nTrace:\n")
      sb.append(format(it))
    }

    sb.toString()

