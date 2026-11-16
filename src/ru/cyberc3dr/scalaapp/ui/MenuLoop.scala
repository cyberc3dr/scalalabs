package ru.cyberc3dr.scalaapp.ui

import ru.cyberc3dr.scalaapp.command.*
import ru.cyberc3dr.scalaapp.ui.UI
import ru.cyberc3dr.scalaapp.utils.Logging
import ru.cyberc3dr.scalaapp.errors.{ExecutionError, Internal, InvalidArgument, MissingArgument, ParsingError, UnknownCommand}
import ru.cyberc3dr.scalaapp.errors.format

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
      case Left(UnknownCommand(name)) =>
        ui.writeError(s"Неизвестная команда: $name. Введите help для списка команд.")
        true

      case Right(spec) =>
        spec.parser.parse(args) match
          case Left(MissingArgument(argument)) =>
            ui.writeError(s"Пропущен аргумент: $argument")
            ui.writeLine(s"Использование: ${spec.usage}")
            true

          case Left(InvalidArgument(value, expected)) =>
            ui.writeError(s"Некорректный аргумент '$value' — ожидалось $expected")
            ui.writeLine(s"Использование: ${spec.usage}")
            true

          case Right(command) =>
            Try(spec.executor.execute(command, CommandContext(reportBuffer))) match
              case Failure(e) =>
                val error = Internal(
                  s"команда '$name': ${e.getClass.getSimpleName}: ${e.getMessage}"
                )
                ui.writeError(error.format)
                true
              case Success(Left(error)) =>
                ui.writeError(error.format)
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
