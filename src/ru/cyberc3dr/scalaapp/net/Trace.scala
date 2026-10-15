package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, Logging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

case class TraceStats(
  address: String,
  hops: List[Hop],
  success: Boolean
)

case class Hop(
  address: String,
  latency: Double
)

def traceroute(address: String, count: Int): TraceStats =
  if !EnvironmentChecker.isAvailable("traceroute") then
    throw IllegalStateException("Команда traceroute недоступна.")

  val cmd = s"traceroute -m $count -q 1 -n $address"
  val result = executeCommand(cmd, "traceroute", address)

  if result.output.exists(_.contains("unknown host")) then
    throw IllegalStateException("Хост не существует.")

  val hops = result.output
    .filter(_.startsWith(" "))
    .map { s =>
      val split = s.split("\\s+")
      val ip = split.find(_.count(_ == '.') == 3) match
        case Some(value) => value
        case None => "*"

      val latency = split.find(_.count(_ == '.') == 1) match
        case Some(value) => Try(value.toDouble).getOrElse(-1.0)
        case None => -1.0

      Hop(ip, latency)
    }

  // надо точно убедиться достигнута ли цель
  var ip = List(address)
  if address.exists(!_.isDigit) then
    // надо понять какой айпи у домена
    val future = Future {
      dig(address)
    }

    ip = Try(Await.result(future, 5.seconds)) match
      case Success(value) => value.addresses
      case Failure(e) =>
        Logging.warn(s"Не удалось разрешить домен для проверки трассировки: $address, ошибка: ${e.getMessage}")
        ip

  val success = ip.contains(hops.last.address)

  Logging.debug(s"Traceroute результат: адрес=$address, переходов=${hops.length}, успех=$success, последний хоп=${hops.lastOption.map(_.address).getOrElse("")}")

  TraceStats(address, hops, success)