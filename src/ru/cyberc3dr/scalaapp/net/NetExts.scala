package ru.cyberc3dr.scalaapp.net

import scala.collection.mutable.ListBuffer
import scala.sys.process.{ProcessLogger, stringToProcess}

// по отдельным файлам ибо этот говнокод стало трудно читать
// scala compiler разберется - это его обязанность

private def executeCommand(cmd: String): List[String] =
  val buf = ListBuffer.empty[String]
  val proc = cmd ! ProcessLogger(
    s => buf += s, s => buf += s
  )

  buf.toList

def getGateway: String =
  val result = executeCommand("route -n get default")
  result.find(_.contains("gateway")) match
    case Some(value) => value.split("\\s+").last
    case None => throw IllegalStateException("Не удалось обнаружить шлюз - вы не подключены к сети или находитесь под VPN")