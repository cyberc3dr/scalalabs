package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, Logging}

case class PingStats(
  address: String,
  packetsSent: Int,
  packetsReceived: Int,
  lossPercent: Double,
  minimalLatency: Double,
  avgLatency: Double,
  maxLatency: Double
)

def ping(address: String, count: Int): PingStats =
  if !EnvironmentChecker.isAvailable("ping") then
    throw IllegalStateException("Команда ping недоступна.")

  val cmd = s"ping -c $count $address"
  val result = executeCommand(cmd, "ping", address)

  if result.output.exists(_.contains("Unknown host")) then
    throw IllegalStateException("Хост не существует.")

  val latencies = result.output
    .filter(_.contains("bytes from"))
    .map(_.split("\\s+")
      .takeRight(2).head // получить два с конца и взять первый слева
      .replace("time=", "")
      .toDouble
    )

  var minimalLatency = -1.0
  var avgLatency = -1.0
  var maxLatency = -1.0

  if (latencies.nonEmpty) {
    minimalLatency = latencies.min
    avgLatency = latencies.sum / latencies.length
    maxLatency = latencies.max
  }

  val stats = result.output(
    result.output.indexWhere(_.contains("ping statistics")) + 1
  ).split(", ")

  val packetsSent = stats(0).split("\\s+").head.toInt
  val packetsReceived = stats(1).split("\\s+").head.toInt
  val lossPercent = stats(2).split("\\s+").head.stripSuffix("%").toDouble

  Logging.debug(s"Ping результат: адрес=$address, отправлено=$packetsSent, получено=$packetsReceived, потеря=$lossPercent%, min=$minimalLatency, avg=$avgLatency, max=$maxLatency")

  PingStats(address, packetsSent, packetsReceived, lossPercent, minimalLatency, avgLatency, maxLatency)