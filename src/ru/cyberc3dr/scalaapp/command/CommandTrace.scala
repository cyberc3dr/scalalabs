package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.net.traceroute
import ru.cyberc3dr.scalaapp.utils.Logging

import scala.util.{Failure, Success, Try}

object CommandTrace extends Command:
  override val name: String = "trace"
  override val usage: String = "trace <адрес> (макс. кол-во хопов)"

  override def execute(ctx: CommandContext): Boolean =
    val rbuf = ctx.reportBuffer
    val buf = ctx.buf
    
    if !buf.hasNext then return false
      
    val address = buf.getString(1)
    val hops = if buf.hasNext then buf.getInt else 15
    
    val result = Try(traceroute(address, hops)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => Logging.error(s"Ошибка traceroute: ${e.getMessage}"); return true
      
    println(result)
    
    rbuf.appendLine(s"=== TRACE: $address (max $hops hops) ===")
    rbuf.appendLine(result)
    rbuf.appendLine("")
    true
