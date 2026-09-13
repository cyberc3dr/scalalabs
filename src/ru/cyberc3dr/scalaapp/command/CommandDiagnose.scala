package ru.cyberc3dr.scalaapp.command

object CommandDiagnose extends Command:
  override val name: String = "diagnose"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf

    if !buf.hasNext then
      println(s"Использование: diagnose <профиль>")


