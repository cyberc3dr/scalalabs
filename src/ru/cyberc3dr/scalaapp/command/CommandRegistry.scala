package ru.cyberc3dr.scalaapp.command

import scala.collection.mutable.ListBuffer

object CommandRegistry:

  private val commands = ListBuffer[Command]()

  def registerCommand(command: Command): Unit = commands += command

  def registerDefaults(): Unit =
    registerCommand(CommandExit)
    registerCommand(CommandTestArgs)
    registerCommand(CommandProfiles)
    registerCommand(CommandReloadConfig)
    registerCommand(CommandPing)
    registerCommand(CommandHttp)
    registerCommand(CommandDns)
    registerCommand(CommandTrace)
    registerCommand(CommandDiagnose)
    registerCommand(CommandHistory)
    registerCommand(CommandCompare)

  def getByName(name: String): Command =
    commands.find(cmd => cmd.name.equalsIgnoreCase(name) || cmd.aliases.contains(name.toLowerCase)) match
      case Some(cmd) => cmd
      case None => throw CommandNotFoundException(name)

end CommandRegistry

// Нужен ли в таком случае exception или лучше придумать какой нибудь Either ?
// Я не разбираюсь, но по моему throw это тяжеловесная операция для JVM
class CommandNotFoundException(commandName: String)
  extends Exception(s"Команда $commandName не найдена в регистре команд.")

