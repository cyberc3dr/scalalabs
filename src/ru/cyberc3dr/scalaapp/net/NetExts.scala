package ru.cyberc3dr.scalaapp.net

import ru.cyberc3dr.scalaapp.utils.Logging

import scala.collection.mutable.ListBuffer
import scala.sys.process.{ProcessLogger, stringToProcess}

// по отдельным файлам ибо этот говнокод стало трудно читать
// scala compiler разберется - это его обязанность

case class CommandResult(
  output: List[String],
  exitCode: Int
)

def executeCommand(cmd: String, utility: String, target: String): CommandResult =
  Logging.debug(s"Запуск внешней команды: $utility, цель: $target, команда: $cmd")

  val buf = ListBuffer.empty[String]
  val proc = cmd ! ProcessLogger(
    s => buf += s, s => buf += s
  )

  Logging.debug(s"Команда завершена: $utility, цель: $target, код выхода: $proc")
  if proc != 0 then
    Logging.warn(s"Внешняя команда завершилась с ненулевым кодом: $utility, цель: $target, код: $proc")

  CommandResult(buf.toList, proc)

def getGateway: String =
  val result = executeCommand("route -n get default", "route", "default gateway")
  result.output.find(_.contains("gateway")) match
    case Some(value) => value.split("\\s+").last
    case None => throw IllegalStateException("Не удалось обнаружить шлюз - вы не подключены к сети или находитесь под VPN")