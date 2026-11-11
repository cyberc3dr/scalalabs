package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal}

object TestArgsParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] =
    for
      number    <- args.getInt
      word      <- args.getString(1)
      remaining = args.getRemaining
    yield TestArgsCommand(number, word, remaining)

object TestArgsExecutor extends CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case TestArgsCommand(number, word, remaining) =>
      val text = s"Введено целое число $number\nПолучено слово $word\nОставшееся: $remaining"
      Right(CommandResult(text))
    case _ => Left(Internal("TestArgsExecutor получил чужую команду"))
