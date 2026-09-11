package ru.cyberc3dr.scalaapp.utils

import scala.collection.mutable.ListBuffer
import scala.sys.process._

case class PingStats(
  address: String,
  packetsSent: Int,
  packetsReceived: Int,
  lossPercent: Double,
  minimalLatency: Double,
  avgLatency: Double,
  maxLatency: Double
)

object NetUtils:

  private def executeCommand(cmd: String) : List[String] =
    val buf = ListBuffer.empty[String]
    val proc = cmd ! ProcessLogger(
      s => buf += s, s => buf += s
    )

    buf.toList

  def ping(address: String, count: Int): PingStats =
    val result = executeCommand(s"ping -c $count $address")

    if(result.exists(_.contains("Unknown host"))) {
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

    if(latencies.nonEmpty) {
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


