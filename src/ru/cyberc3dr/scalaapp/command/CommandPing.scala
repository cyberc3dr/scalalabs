package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

import scala.util.{Try, Success, Failure}

object CommandPing extends Command:
  override val name: String = "ping"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf

    if(!buf.hasNext) {
      println("Использование: ping <адрес> (количество пингов)")
      return
    }

    val address = buf.getString(1)
    val pings = if buf.hasNext then buf.getInt else 10
    
    val result = Try(NetUtils.ping(address, pings)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => println(s"Ошибка: ${e.getMessage}"); return
      
    println(result)