package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.errors.{AppError, ConfigNotLoaded, Internal, MissingArgument, ProfileNotFound}
import ru.cyberc3dr.scalaapp.net.diag
import ru.cyberc3dr.scalaapp.utils.{HistoryManager, Logging}

object DiagnoseParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] =
    if !args.hasNext then Left(MissingArgument("профиль"))
    else args.getString(1).map(DiagnoseCommand(_))

object DiagnoseExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case DiagnoseCommand(profileName) =>
      App.config match
        case None => Left(ConfigNotLoaded)
        case Some(config) =>
          config.profiles.get(profileName) match
            case None => Left(ProfileNotFound(profileName))
            case Some(profile) =>
              val diagnostic = diag(profile)
              val formatted = Logging.format(diagnostic)

              HistoryManager.save(diagnostic, profileName).map { id =>
                Logging.info(s"Диагностика сохранена: id=$id")
                CommandResult(
                  s"$formatted\nДиагностика сохранена: id=$id",
                  appendToReport = true
                )
              }
    case _ => Left(Internal("DiagnoseExecutor получил чужую команду"))
