package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.AppError

/** Разбор аргументов ввода в команду. */
trait CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command]

/** Исполнение разобранной команды. */
trait CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal]

/** Результат выполнения команды. */
sealed trait CommandSignal

/** Текст для пользователя; при appendToReport он дописывается в отчёт. */
final case class CommandResult(text: String, appendToReport: Boolean = false) extends CommandSignal

/** Сигнал завершения работы. */
case object ExitSignal extends CommandSignal

/** Составные части команды, спецификация */
final case class CommandSpec(
  name: String,
  usage: String,
  description: String = "",
  aliases: Seq[String] = Seq.empty,
  parser: CommandParser,
  executor: CommandExecutor
):

  def matches(token: String): Boolean =
    name.equalsIgnoreCase(token) || aliases.contains(token.toLowerCase)

/** Контекст исполнения */
case class CommandContext(reportBuffer: ReportBuffer)
