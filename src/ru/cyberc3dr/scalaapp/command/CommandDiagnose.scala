package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App

object CommandDiagnose extends Command:
  override val name: String = "diagnose"
  override val usage: String = "diagnose <профиль>"
  override val aliases: Seq[String] = Seq("diag")

  override def execute(ctx: CommandContext): Boolean =
    // костыль продакшн представляет фильм
    // как сделать диагностику монолитно и в одном файле
    // ну я просто хз как по нормальному сделать

    val buf = ctx.buf

    if !buf.hasNext then return false

    val config = App.config match
      case Some(value) => value
      case None => println("Ошибка: Конфигурация не загружена. Попробуйте перезагрузить."); return true

    val profileName = buf.getString(1)
    val profile = config.profiles.get(profileName) match
      case Some(value) => value
      case None => println(s"Ошибка: профиль с именем $profileName не найден."); return true

    

    true


