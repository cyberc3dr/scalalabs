package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

object CommandHttp extends Command:
  override val name: String = "http"
  override val usage: String = "http <адрес>"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf

    if !buf.hasNext then return false

    val address = buf.getString
    val result = NetUtils.curl(address)
    
    println(Logging.format(result))
    true

