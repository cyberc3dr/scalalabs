package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App

object CommandReloadConfig extends Command:
  override val name: String = "reload"

  override def execute(ctx: CommandContext): Unit = {
    println("Перезагрузка конфигурации...")
    App.reloadConfiguration()
    println("Конфигурация перезагружена.")
  }
