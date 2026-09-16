package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App

object CommandDiagnose extends Command:
  override val name: String = "diagnose"
  override val usage: String = "diagnose <профиль>"

  override def execute(ctx: CommandContext): Boolean =
    // костыль продакшн представляет фильм
    // как сделать диагностику монолитно и в одном файле
    // ну я просто хз как по нормальному сделать

    val buf = ctx.buf

    if !buf.hasNext then return false

    val config = App.config

    val profile = buf.getString(1)


      
    true


