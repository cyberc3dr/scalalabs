package ru.cyberc3dr.scalaapp.utils

import scala.collection.mutable
import sys.process._

object EnvironmentChecker {

  private val cmds = List("ifconfig", "route", "ping", "nslookup", "dig", "traceroute", "curl")
  private val availability = mutable.Map.empty[String, Boolean]

  def check(): Unit = {
    cmds.foreach { cmd =>
      // молчать, код завершения 0 - команда найдена
      // одна из лучших фич за сегодня
      val output = s"which ${cmd}".!(ProcessLogger(_ => ()))
      val isAvailable = output == 0

      availability(cmd) = isAvailable
    }

    // todo - убрать этот пример вызова
    val curl = s"curl --head --location --max-time 10 https://example.com".!!(ProcessLogger(_ => ()))
    println(curl)
  }

  def isAvailable(cmd: String): Boolean = availability.getOrElse(cmd, throw IllegalArgumentException("Команда введена неверно"))
}
