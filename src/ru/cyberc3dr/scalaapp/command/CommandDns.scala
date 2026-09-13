package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object CommandDns extends Command:
  override val name: String = "dns"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf
    
    if !buf.hasNext then
      println("Использование: dns <адрес>")
      return
      
    val address = buf.getString

    val future = Future {
      NetUtils.dig(address)
    }

    val result = Try(Await.result(future, 5.seconds)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => println(s"Ошибка: ${e.getMessage}"); return

    println(result)
