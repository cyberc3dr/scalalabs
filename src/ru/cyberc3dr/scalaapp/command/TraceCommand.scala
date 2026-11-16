package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{ExecutionError, ParsingError, Internal, MissingArgument}
import ru.cyberc3dr.scalaapp.net.traceroute
import ru.cyberc3dr.scalaapp.utils.Logging

object TraceParser extends CommandParser:
  def parse(args: StrBuffer): Either[ParsingError, Command] =
    if !args.hasNext then return Left(MissingArgument("адрес"))
    for
      address <- args.getString(1)
      hops <- args.getIntOrElse(15)
    yield TraceCommand(address, hops)

object TraceExecutor extends CommandExecutor:
  def execute(command: Command, ctx: CommandContext): Either[ExecutionError, CommandSignal] = command match
    case TraceCommand(address, maxHops) =>
      traceroute(address, maxHops).map { stats =>
        val formatted = Logging.format(stats)
        CommandResult(s"=== TRACE: $address (max $maxHops hops) ===\n$formatted", appendToReport = true)
      }
    case _ => Left(Internal("TraceExecutor получил чужую команду"))
