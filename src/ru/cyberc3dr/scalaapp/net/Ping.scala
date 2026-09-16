package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.utils.EnvironmentChecker

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

  val result = executeCommand(s"ping -c $count $address")

  if (result.exists(_.contains("Unknown host"))) {
    throw IllegalStateException("Хост не существует.")
  }

  val latencies = result
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

  val stats = result(
    result.indexWhere(_.contains("ping statistics")) + 1
  ).split(", ")

  val packetsSent = stats(0).split("\\s+").head.toInt
  val packetsReceived = stats(1).split("\\s+").head.toInt
  val lossPercent = stats(2).split("\\s+").head.stripSuffix("%").toDouble

  PingStats(address, packetsSent, packetsReceived, lossPercent, minimalLatency, avgLatency, maxLatency)