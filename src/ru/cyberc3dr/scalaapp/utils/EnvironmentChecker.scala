package ru.cyberc3dr.scalaapp.utils

import scala.collection.mutable
import sys.process._

object EnvironmentChecker {

  private val cmds = List("ifconfig", "route", "ping", "nslookup", "dig", "traceroute", "curl")
  private val availability = mutable.Map.empty[String, Boolean]

  def check(): Boolean = {
    if !checkMacOS() then return false

    cmds.foreach { cmd =>
      val output = s"which ${cmd}".!(ProcessLogger(_ => ()))
      val isAvailable = output == 0
      availability(cmd) = isAvailable
    }
    true
  }

  private def checkMacOS(): Boolean = {
    val os = System.getProperty("os.name").toLowerCase
    if !os.contains("mac") then
      println("Ошибка: Это приложение работает только на macOS")
      false
    else
      true
  }

  def isAvailable(cmd: String): Boolean = availability.get(cmd) match
    case Some(value) => value
    case None => throw IllegalArgumentException("Команда введена неверно")
}
