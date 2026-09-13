package ru.cyberc3dr.scalaapp.command

object CommandExit extends Command:

  override val name: String = "exit"

  override def execute(ctx: CommandContext): Boolean =
    println("Выключение...")
    System.exit(0)
    true
