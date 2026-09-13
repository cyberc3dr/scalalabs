package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

import scala.util.{Try, Success, Failure}

object CommandTrace extends Command:
  override val name: String = "trace"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf
    
    if !buf.hasNext then
      println("Использование: trace <адрес> (макс. кол-во хопов)")
      return
      
    val address = buf.getString(1)
    val hops = if buf.hasNext then buf.getInt else 15
    
    val result = Try(NetUtils.traceroute(address, hops)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => println(s"Ошибка: ${e.getMessage}"); return
      
    println(result)
