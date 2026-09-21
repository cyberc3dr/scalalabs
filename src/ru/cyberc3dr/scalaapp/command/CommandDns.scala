package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.net.dig
import ru.cyberc3dr.scalaapp.utils.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object CommandDns extends Command:
  override val name: String = "dns"
  override val usage: String = "dns <адрес>"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf
    
    if !buf.hasNext then return false
      
    val address = buf.getString

    val future = Future {
      dig(address)
    }

    val result = Try(Await.result(future, 5.seconds)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => Logging.error(s"Ошибка DNS: ${e.getMessage}"); return true

    println(result)
    true
