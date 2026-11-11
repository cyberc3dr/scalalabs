package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.errors.{AppError, ConfigNotLoaded, Internal, IoFailure, ReportEmpty}
import ru.cyberc3dr.scalaapp.utils.Logging

import java.io.{File, FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object ReportParser extends CommandParser:
  def parse(args: StrBuffer): Either[AppError, Command] =
    Right(ReportCommand(if args.hasNext then Some(args.getRemaining) else None))

object ReportExecutor extends CommandExecutor:

  def execute(command: Command, ctx: CommandContext): Either[AppError, CommandSignal] = command match
    case ReportCommand(fileName) =>
      App.config match
        case None => Left(ConfigNotLoaded)
        case Some(config) =>
          val name = fileName.getOrElse {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            s"report_$timestamp.txt"
          }
          
          val reportDir = config.reportDirectory
          val dir = File(reportDir)
          if !dir.exists() then dir.mkdirs()

          val filePath = s"$reportDir${File.separator}$name"

          val content = ctx.reportBuffer.getContent
          if content.isEmpty then return Left(ReportEmpty)

          Try {
            val writer = PrintWriter(FileWriter(filePath))
            try
              writer.write(content)
              ctx.reportBuffer.clear()
            finally
              writer.close()
          }.toEither
            .left.map(e => IoFailure(filePath, e.getMessage))
            .map { _ =>
              Logging.info(s"Отчет сохранен: $filePath")
              CommandResult(s"Отчет сохранен: $filePath")
            }
    case _ => Left(Internal("ReportExecutor получил чужую команду"))
