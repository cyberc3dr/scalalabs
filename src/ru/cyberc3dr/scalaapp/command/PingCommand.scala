package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal, MissingArgument}
import ru.cyberc3dr.scalaapp.net.ping
import ru.cyberc3dr.scalaapp.utils.Logging

object PingParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] =
    if !args.hasNext then return Left(MissingArgument("адрес"))
    for
      address <- args.getString(1)
      count <- args.getIntOrElse(10)
    yield PingCommand(address, count)

object PingExecutor extends CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case PingCommand(address, count) =>
      ping(address, count).map { stats =>
        val formatted = Logging.format(stats)
        CommandResult(s"=== PING: $address ($count packets) ===\n$formatted", appendToReport = true)
      }
    case _ => Left(Internal("PingExecutor получил чужую команду"))
