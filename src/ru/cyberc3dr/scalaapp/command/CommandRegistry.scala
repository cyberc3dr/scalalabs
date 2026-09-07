package ru.cyberc3dr.scalaapp.command

import scala.collection.mutable.ListBuffer

object CommandRegistry:

  private val commands = ListBuffer[Command]()

  def registerCommand(command: Command): Unit = commands += command

  def registerDefaults(): Unit =
    registerCommand(CommandExit)
    registerCommand(CommandTestArgs)

  def getByName(name: String): Command =
    commands.find(_.name.equalsIgnoreCase(name))
      .getOrElse(throw CommandNotFoundException(name))

end CommandRegistry

class CommandNotFoundException(commandName: String)
  extends Exception(s"Команда $commandName не найдена в регистре команд.")

