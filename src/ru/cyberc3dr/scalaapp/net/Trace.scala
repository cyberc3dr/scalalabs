package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.utils.EnvironmentChecker

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

  val result = executeCommand(s"traceroute -m $count -q 1 -n $address")

  if result.exists(_.contains("unknown host")) then
    throw IllegalStateException("Хост не существует.")

  val hops = result
    .filter(_.startsWith(" "))
    .map { s =>
      val split = s.split("\\s+")
      val ip = split.find(_.count(_ == '.') == 3) match
        case Some(value) => value
        case None => "*"

      val latency = split.find(_.count(_ == '.') == 1) match
        case Some(value) => value.toDouble
        case None => -1.0

      Hop(ip, latency)
    }

  // надо точно убедиться достигнута ли цель
  var ip = List(address)
  if address.exists(!_.isDigit) then
    // надо понять какой айпи у домена
    ip = dig(address).addresses

  val success = ip.contains(hops.last.address)

  TraceStats(address, hops, success)