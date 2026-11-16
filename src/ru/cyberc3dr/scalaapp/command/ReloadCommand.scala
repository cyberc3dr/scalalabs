package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{ConfigReloadFailed, ExecutionError, ParsingError, Internal}
import ru.cyberc3dr.scalaapp.errors.format
import ru.cyberc3dr.scalaapp.App

object ReloadParser extends CommandParser:
  def parse(args: StrBuffer): Either[ParsingError, Command] = Right(ReloadCommand)

object ReloadExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[ExecutionError, CommandSignal] = command match
    case ReloadCommand =>
      App.reloadConfiguration() match
        case Right(_) => Right(CommandResult("Конфигурация перезагружена."))
        case Left(error) => Left(ConfigReloadFailed(error.format))
    case _ => Left(Internal("ReloadExecutor получил чужую команду"))
