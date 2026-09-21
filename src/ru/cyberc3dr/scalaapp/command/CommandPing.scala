package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.net.ping
import ru.cyberc3dr.scalaapp.utils.Logging

import scala.util.{Failure, Success, Try}

object CommandPing extends Command:
  override val name: String = "ping"
  override val usage: String = "ping <адрес> (количество пингов)"

  override def execute(ctx: CommandContext): Boolean =
    val rbuf = ctx.reportBuffer
    val buf = ctx.buf

    if !buf.hasNext then return false

    val address = buf.getString(1)
    val pings = if buf.hasNext then buf.getInt else 10
    
    val result = Try(ping(address, pings)) match
      case Success(value) => Logging.format(value)
      case Failure(e) => Logging.error(s"Ошибка ping: ${e.getMessage}"); return true
      
    println(result)

    rbuf.appendLine(s"=== PING: $address ($pings packets) ===")
    rbuf.appendLine(result)
    rbuf.appendLine("")
    true