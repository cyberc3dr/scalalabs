package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{ExecutionError, ParsingError, Internal}

object ExitParser extends CommandParser:
  def parse(args: StrBuffer): Either[ParsingError, Command] = Right(ExitCommand)

object ExitExecutor extends CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[ExecutionError, CommandSignal] = command match
    case ExitCommand => Right(ExitSignal)
    case _ => Left(Internal("ExitExecutor получил чужую команду"))
