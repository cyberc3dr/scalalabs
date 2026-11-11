package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, Internal}
import ru.cyberc3dr.scalaapp.App

object ReloadParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] = Right(ReloadCommand)

object ReloadExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case ReloadCommand =>
      App.reloadConfiguration().map(_ => CommandResult("Конфигурация перезагружена."))
    case _ => Left(Internal("ReloadExecutor получил чужую команду"))
