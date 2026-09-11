package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.{Logging, NetUtils}

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

    try {
      val result = NetUtils.ping(address, pings)
      println(Logging.format(result))
    } catch
      case e: IllegalStateException => println(s"Ошибка: ${e.getMessage}")