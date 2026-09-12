package ru.cyberc3dr.scalaapp.utils

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import ru.cyberc3dr.scalaapp.model.JsonParser

import scala.collection.mutable.ListBuffer
import scala.sys.process.*

case class PingStats(
  address: String,
  packetsSent: Int,
  packetsReceived: Int,
  lossPercent: Double,
  minimalLatency: Double,
  avgLatency: Double,
  maxLatency: Double
)

@JsonIgnoreProperties(ignoreUnknown = true) // Нам нужны не все поля
case class CurlResult(
  @JsonProperty("url") url: String,
  @JsonProperty("time_total") timeTotal: Double,
  @JsonProperty("http_code") httpCode: Int,
  @JsonProperty("num_redirects") redirects: Int,
  @JsonProperty("url_effective") urlEffective: String,
  @JsonProperty("ssl_verify_result") sslVerifyResult: Int
) {
  def timeTotalMs: Double = timeTotal * 1000
  def isAvailable: Boolean = httpCode > 0
  def hasTlsError: Boolean = sslVerifyResult != 0
}

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

  def curl(address: String): CurlResult =
    val result = executeCommand(s"curl -sL -o /dev/null -w \"%{json}\" $address")
    val json = result.head

    JsonParser.fromJson[CurlResult](json)


