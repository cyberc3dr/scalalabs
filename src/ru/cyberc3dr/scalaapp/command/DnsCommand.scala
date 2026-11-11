package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal, MissingArgument, OperationTimeout}
import ru.cyberc3dr.scalaapp.net.dig
import ru.cyberc3dr.scalaapp.utils.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import java.util.concurrent.TimeoutException

object DnsParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] =
    if !args.hasNext then Left(MissingArgument("адрес"))
    else Right(DnsCommand(args.getRemaining))

object DnsExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case DnsCommand(address) =>
      val future = Future { dig(address) }

      Try(Await.result(future, 5.seconds)) match
        case Success(Right(stats)) =>
          Right(CommandResult(s"=== DNS: $address ===\n${Logging.format(stats)}", appendToReport = true))
        case Success(Left(error)) => Left(error)
        case Failure(_: TimeoutException) => Left(OperationTimeout)
        case Failure(e) => Left(Internal(s"DNS запрос прерван: ${e.getMessage}"))
    case _ => Left(Internal("DnsExecutor получил чужую команду"))
