package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.utils.EnvironmentChecker

import scala.collection.mutable.ListBuffer

case class DnsStats(
  address: String,
  success: Boolean,
  addresses: List[String],
  time: Int
)

def dig(address: String): DnsStats =
  if !EnvironmentChecker.isAvailable("dig") then
    throw IllegalStateException("Команда dig недоступна.")

  val result = executeCommand(s"dig $address")

  val status = result.find(_.contains("->>HEADER<<-")) match
    case Some(value) => value.split(", ")(1).stripPrefix("status: ")
    case None => throw IllegalStateException("DNS запрос не удался")

  val success = status != "NXDOMAIN"
  val addresses = ListBuffer.empty[String]
  if success then
    val answerStart = result.indexWhere(_.contains("ANSWER SECTION")) + 1
    val answerEnd = result.indexWhere(_.contains("Query time")) - 1

    result.slice(answerStart, answerEnd)
      .filter(_.contains("A"))
      .map(_.split("\\s+").last)
      .foreach(addresses += _)

  val time = result.find(_.contains("Query time")) match
    case Some(value) => value.split("\\s+").takeRight(2).head.toInt
    case None => -1

  DnsStats(address, success, addresses.toList, time)