package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App

object CommandReloadConfig extends Command:
  override val name: String = "reload"
  override val aliases: Seq[String] = Seq("reloadconfig", "rlconfig")

  override def execute(ctx: CommandContext): Boolean = {
    println("Перезагрузка конфигурации...")
    App.reloadConfiguration()
    println("Конфигурация перезагружена.")
    true
  }
