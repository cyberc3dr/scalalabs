package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.net.curl
import ru.cyberc3dr.scalaapp.utils.Logging

import scala.util.{Failure, Success, Try}

object CommandHttp extends Command:
  override val name: String = "http"
  override val usage: String = "http <адрес>"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf

    if !buf.hasNext then return false

    val address = buf.getString
    val result = Try(curl(address)) match
      case Success(value) => value
      case Failure(e) => Logging.error(s"Ошибка HTTP: ${e.getMessage}"); return true
    
    println(Logging.format(result))
    true

