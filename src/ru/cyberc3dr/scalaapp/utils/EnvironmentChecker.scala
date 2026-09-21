package ru.cyberc3dr.scalaapp.utils

import scala.collection.mutable
import sys.process._

object EnvironmentChecker {

  private val cmds = List("ifconfig", "route", "ping", "nslookup", "dig", "traceroute", "curl")
  private val availability = mutable.Map.empty[String, Boolean]

  def check(): Boolean = check(true)

  def check(printToConsole: Boolean): Boolean = {
    if !checkMacOS(printToConsole) then return false

    val results = cmds.map { cmd =>
      val output = s"which ${cmd}".!(ProcessLogger(_ => ()))
      val isAvailable = output == 0
      availability(cmd) = isAvailable
      (cmd, isAvailable)
    }

    if printToConsole then {
      println("Проверка системных утилит:")
      println()
      results.foreach { case (cmd, available) =>
        val status = if available then "[+]" else "[-]"
        println(s"$status $cmd")
      }
      println()

      val missing = results.collect { case (cmd, false) => cmd }
      missing.foreach { cmd =>
        println(s"Проверка ${getCheckName(cmd)} будет недоступна:")
        println(s"утилита $cmd не найдена.")
        println()
      }
    }

    val (available, missing) = results.partition { case (_, isAvailable) => isAvailable }
    val availableCount = available.size
    val missingCount = missing.size
    Logging.debug(s"Проверка утилит завершена: available=$availableCount, missing=$missingCount")

    true
  }

  private def checkMacOS(printToConsole: Boolean): Boolean = {
    val os = System.getProperty("os.name").toLowerCase
    if !os.contains("mac") then
      if printToConsole then println("Ошибка: Это приложение работает только на macOS")
      false
    else
      true
  }

  private def getCheckName(cmd: String): String = cmd match
    case "traceroute" => "маршрута"
    case "dig" | "nslookup" => "DNS"
    case "curl" => "HTTP"
    case "ping" => "доступности"
    case "ifconfig" | "route" => "сетевых интерфейсов"
    case _ => cmd

  def isAvailable(cmd: String): Boolean = availability.get(cmd) match
    case Some(value) => value
    case None => throw IllegalArgumentException("Команда введена неверно")
}
