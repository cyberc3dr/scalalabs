package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

object CommandDns extends Command:
  override val name: String = "dns"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf
    
    if !buf.hasNext then
      println("Использование: dns <адрес>")
      return
      
    val address = buf.getString
    try {
      val result = NetUtils.dig(address)
      println(Logging.format(result))
    } catch
      case e: IllegalStateException => println(s"Ошибка: ${e.getMessage}")
