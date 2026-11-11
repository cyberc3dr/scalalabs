package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal}
import ru.cyberc3dr.scalaapp.utils.HistoryManager

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HistoryParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] = Right(HistoryCommand)

object HistoryExecutor extends CommandExecutor:

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case HistoryCommand =>
      val entries = HistoryManager.getAll
      val text =
        if entries.isEmpty then "История пуста."
        else
          val builder = StringBuilder("Сохранённые диагностики:")
          entries.foreach { e =>
            builder.append(
              s"\n  ${e.id}   ${formatter.format(e.time)}  профиль=${e.profile}  состояние=${e.result.state.name}"
            )
          }
          builder.toString()

      Right(CommandResult(text))
    case _ => Left(Internal("HistoryExecutor получил чужую команду"))
