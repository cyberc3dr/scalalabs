package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

object CommandHttp extends Command:
  override val name: String = "http"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf

    if !buf.hasNext then
      println("Использование: http <адрес>")
      return

    val address = buf.getString
    val result = NetUtils.curl(address)
    
    println(Logging.format(result))

