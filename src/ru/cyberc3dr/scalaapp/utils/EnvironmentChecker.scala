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
  }

  def isAvailable(cmd: String): Boolean = availability.get(cmd) match
    case Some(value) => value
    case None => throw IllegalArgumentException("Команда введена неверно")
}
