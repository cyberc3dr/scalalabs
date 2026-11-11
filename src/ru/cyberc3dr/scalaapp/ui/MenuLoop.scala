package ru.cyberc3dr.scalaapp.ui

import ru.cyberc3dr.scalaapp.command.*
import ru.cyberc3dr.scalaapp.ui.UI
import ru.cyberc3dr.scalaapp.utils.Logging
import ru.cyberc3dr.scalaapp.errors.{Internal, UnknownCommand}

import scala.util.{Failure, Success, Try}

final class MenuLoop(ui: UI, reportBuffer: ReportBuffer):

  def run(): Unit =
    Logging.info("Command line interface started.")
    var running = true
    
    while running do
      ui.readLine() match
        case None => running = false
        case Some(line) if line.trim.nonEmpty => running = handle(line)
        case Some(_) => ()

  /** Разбирает и выполняет одну строку */
  private def handle(line: String): Boolean =
    val tokens = line.trim.split("\\s+")
    val name = tokens.head
    val args = StrBuffer(tokens.tail)

    CommandRegistry.findByName(name) match
      case None =>
        ui.writeError(UnknownCommand(name).message)
        true

      case Some(spec) =>
        spec.parser.parse(args) match
          case Left(error) =>
            ui.writeError(s"${error.message}\nИспользование: ${spec.usage}")
            true

          case Right(command) =>
            Try(spec.executor.execute(command, CommandContext(reportBuffer))) match
              case Failure(e) =>
                val error = Internal(
                  s"команда '$name': ${e.getClass.getSimpleName}: ${e.getMessage}"
                )
                ui.writeError(error.message)
                true
              case Success(Left(error)) =>
                ui.writeError(error.message)
                true
              case Success(Right(ExitSignal)) =>
                ui.writeLine("Выключение...")
                false
              case Success(Right(CommandResult(text, appendToReport))) =>
                ui.writeLine(text)
                if appendToReport then
                  reportBuffer.appendLine(text)
                  reportBuffer.appendLine("")
                true

end MenuLoop
