package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.{CommandContext, CommandNotFoundException, CommandRegistry, ReportBuffer, StrBuffer}

import java.util.Scanner

// jline это не для меня.
object InputHandler:

  def start(reportBuffer: ReportBuffer): Unit =
    println("Command line interface started.")
    val scanner = Scanner(System.in)

    while true do
      val str = scanner.nextLine()

      if str.trim.nonEmpty then
        val buf = StrBuffer(str.split("\\s+"))
        handleCommand(buf.getString(1), CommandContext(buf, scanner, reportBuffer))

  private def handleCommand(name: String, ctx: CommandContext): Unit =
    try {
      val command = CommandRegistry.getByName(name)
      if !command.execute(ctx) then println(s"Использование: ${command.usage}")
    } catch
      case e: CommandNotFoundException =>
        println(e.getMessage)
      case e: Exception =>
        val exceptionClass = e.getClass.getSimpleName
        println(s"Произошла ошибка при выполнении команды $name:")
        e.printStackTrace()

end InputHandler

