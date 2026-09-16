package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.net.traceroute
import ru.cyberc3dr.scalaapp.utils.Logging

import scala.util.{Failure, Success, Try}

object CommandTrace extends Command:
  override val name: String = "trace"
  override val usage: String = "trace <адрес> (макс. кол-во хопов)"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf
    
    if !buf.hasNext then return false
      
    val address = buf.getString(1)
    val hops = if buf.hasNext then buf.getInt else 15
    
    val result = Try(traceroute(address, hops)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => println(s"Ошибка: ${e.getMessage}"); return true
      
    println(result)
    true
