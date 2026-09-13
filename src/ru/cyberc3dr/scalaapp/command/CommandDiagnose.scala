package ru.cyberc3dr.scalaapp.command

object CommandDiagnose extends Command:
  override val name: String = "diagnose"
  override val usage: String = "diagnose <профиль>"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf

    if !buf.hasNext then return false
      
    true


