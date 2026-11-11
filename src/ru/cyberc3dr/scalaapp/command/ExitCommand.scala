package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal}

object ExitParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] = Right(ExitCommand)

object ExitExecutor extends CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case ExitCommand => Right(ExitSignal)
    case _ => Left(Internal("ExitExecutor получил чужую команду"))
