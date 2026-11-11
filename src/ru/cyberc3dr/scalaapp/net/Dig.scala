package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.errors.{AppError, OutputParseFailed, UtilityUnavailable}
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, Logging}

import scala.collection.mutable.ListBuffer

case class DnsStats(
  address: String,
  success: Boolean,
  addresses: List[String],
  time: Int
)

def dig(address: String): Either[AppError, DnsStats] =
  if !EnvironmentChecker.isAvailable("dig") then return Left(UtilityUnavailable("dig"))

  val cmd = s"dig $address"
  val result = executeCommand(cmd, "dig", address)

  val status = result.output
    .find(_.contains("->>HEADER<<-"))
    .flatMap(_.split(", ").lift(1))
    .map(_.stripPrefix("status: "))

  status match
    case None =>
      Logging.error(s"Не удалось разобрать вывод dig: цель=$address, вывод=${result.output.take(5).mkString("; ")}")
      Left(OutputParseFailed("dig", address))

    case Some(status) =>
      val success = status != "NXDOMAIN"
      val addresses = ListBuffer.empty[String]
      if success then
        val answerStart = result.output.indexWhere(_.contains("ANSWER SECTION")) + 1
        val answerEnd = result.output.indexWhere(_.contains("Query time")) - 1

        result.output.slice(answerStart, answerEnd)
          .filter(_.contains("A"))
          .map(_.split("\\s+").last)
          .foreach(addresses += _)

      val time = result.output
        .find(_.contains("Query time"))
        .flatMap(_.split("\\s+").takeRight(2).headOption)
        .flatMap(_.toIntOption)
        .getOrElse(-1)

      Logging.debug(s"Dig результат: адрес=$address, успех=$success, адреса=${addresses.mkString(",")}, время=$time мс, статус=$status")

      Right(DnsStats(address, success, addresses.toList, time))
