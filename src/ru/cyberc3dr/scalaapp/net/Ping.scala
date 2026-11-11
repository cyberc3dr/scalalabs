package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.errors.{AppError, HostNotFound, OutputParseFailed, UtilityUnavailable}
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

def ping(address: String, count: Int): Either[AppError, PingStats] =
  if !EnvironmentChecker.isAvailable("ping") then return Left(UtilityUnavailable("ping"))

  val cmd = s"ping -c $count $address"
  val result = executeCommand(cmd, "ping", address)

  if result.output.exists(_.contains("Unknown host")) then return Left(HostNotFound(address))

  val latencies = result.output
    .filter(_.contains("bytes from"))
    .flatMap(parseLatency)

  val (minimalLatency, avgLatency, maxLatency) =
    if latencies.nonEmpty
    then (latencies.min, latencies.sum / latencies.length, latencies.max)
    else (-1.0, -1.0, -1.0)

  parseStatistics(result.output) match
    case Some((packetsSent, packetsReceived, lossPercent)) =>
      Logging.debug(s"Ping результат: адрес=$address, отправлено=$packetsSent, получено=$packetsReceived, потеря=$lossPercent%, min=$minimalLatency, avg=$avgLatency, max=$maxLatency")

      Right(PingStats(address, packetsSent, packetsReceived, lossPercent, minimalLatency, avgLatency, maxLatency))

    case None => Left(OutputParseFailed("ping", address))

// "time=21.441" -> 21.441
def parseLatency(line: String): Option[Double] =
    line.split("\\s+")
      .takeRight(2)
      .headOption
      .map(_.replace("time=", ""))
      .flatMap(_.toDoubleOption)

// строка после "ping statistics" вида
// "10 packets transmitted, 10 packets received, 0.0% packet loss" -> (10, 10, 0.0)
def parseStatistics(output: List[String]): Option[(Int, Int, Double)] =
    val statsLine = output.indexWhere(_.contains("ping statistics")) match
      case -1 => None
      case i  => output.lift(i + 1).map(_.split(", "))

    for
      line <- statsLine
      sent     <- line.headOption.flatMap(parseFirstInt)
      received <- line.lift(1).flatMap(parseFirstInt)
      loss     <- line.lift(2).flatMap(parseLossPercent)
    yield (sent, received, loss)

def parseFirstInt(part: String): Option[Int] =
    part.split("\\s+").headOption.flatMap(_.toIntOption)

def parseLossPercent(part: String): Option[Double] =
    part.split("\\s+").headOption.map(_.stripSuffix("%")).flatMap(_.toDoubleOption)
