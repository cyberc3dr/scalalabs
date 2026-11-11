package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal, MissingArgument}
import ru.cyberc3dr.scalaapp.net.curl
import ru.cyberc3dr.scalaapp.utils.Logging

object HttpParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] =
    if !args.hasNext then Left(MissingArgument("адрес"))
    else Right(HttpCommand(args.getRemaining))

object HttpExecutor extends CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case HttpCommand(address) =>
      curl(address).map { result =>
        CommandResult(s"=== HTTP: $address ===\n${Logging.format(result)}", appendToReport = true)
      }
    case _ => Left(Internal("HttpExecutor получил чужую команду"))
