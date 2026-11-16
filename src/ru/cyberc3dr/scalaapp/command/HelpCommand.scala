package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{ExecutionError, Internal, ParsingError, UnknownCommand}

object HelpParser extends CommandParser:
  def parse(args: StrBuffer): Either[ParsingError, Command] =
    val query = if args.hasNext then Some(args.getString(1).getOrElse("")) else None
    Right(HelpCommand(query))

object HelpExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[ExecutionError, CommandSignal] = command match
    case HelpCommand(query) =>
      query match
        case None => Right(CommandResult(formatAll()))
        case Some(q) =>
          CommandRegistry.findByName(q) match
            case Right(spec) => Right(CommandResult(formatSpec(spec)))
            case Left(UnknownCommand(name)) => Right(CommandResult(s"Команда '$name' не найдена.\n${formatAll()}"))
    case _ => Left(Internal("HelpExecutor получил чужую команду"))

  private def formatAll(): String =
    val commands = CommandRegistry.getAll.sortBy(_.name)
    val width = commands.map(_.name.length).maxOption.getOrElse(0)

    val sb = StringBuilder(s"Доступные команды (${commands.size}):\n")
    sb.append("-" * 60 + "\n")
    commands.foreach { cmd =>
      val aliases = if cmd.aliases.isEmpty then "" else s" [aliases: ${cmd.aliases.mkString(", ")}]"
      sb.append(s" ${cmd.name.padTo(width, ' ')} - ${cmd.description}$aliases\n")
    }
    sb.append("-" * 60 + "\n")
    sb.append("Подробнее о команде: help <команда>")
    sb.toString

  private def formatSpec(spec: CommandSpec): String =
    val sb = StringBuilder(s"Команда: ${spec.name}\n")
    if spec.aliases.nonEmpty then sb.append(s"aliases: ${spec.aliases.mkString(", ")}\n")
    sb.append(s"Использование: ${spec.usage}\n")
    if spec.description.nonEmpty then sb.append(s"Описание: ${spec.description}")
    sb.toString
