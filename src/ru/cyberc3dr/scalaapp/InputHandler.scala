package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.{CommandNotFoundException, CommandRegistry, StrBuffer}

import java.util.Scanner

object InputHandler:

  def start() : Unit =
    println("Command line interface started.")
    val scanner = Scanner(System.in)

    while true do
      val str = scanner.nextLine()

      if str.trim.nonEmpty then
        val buf = StrBuffer(str.split("\\s+"))
        handleCommand(buf.getString(1), buf)

  private def handleCommand(name: String, buf: StrBuffer): Unit =
    try {
      val command = CommandRegistry.getByName(name)
      command.execute(buf)
    } catch
      case e: CommandNotFoundException =>
        println(e.getMessage)
      case e: Exception =>
        val exceptionClass = e.getClass.getSimpleName
        println(s"Произошла ошибка при выполнении команды $name:")
        println(s"$exceptionClass: ${e.getMessage}")

end InputHandler

