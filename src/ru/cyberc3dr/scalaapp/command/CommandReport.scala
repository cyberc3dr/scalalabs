package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.utils.Logging

import java.io.{File, FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object CommandReport extends Command:
  override val name: String = "report"
  override val usage: String = "report [имя_файла]"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf
    
    val fileName = if buf.hasNext then buf.getString else
      val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
      s"report_$timestamp.txt"

    val config = App.config match
      case Some(value) => value
      case None => Logging.error("Конфигурация не загружена. Попробуйте перезагрузить."); return true

    val reportDir = config.reportDirectory
    val dir = File(reportDir)
    if !dir.exists() then dir.mkdirs()

    val filePath = s"$reportDir${File.separator}$fileName"

    val rbuf = ctx.reportBuffer
    val content = rbuf.getContent
    if content.isEmpty then
      println("Отчет пуст - нет выполненных сетевых команд")
      return true

    Try {
      val writer = PrintWriter(FileWriter(filePath))
      try
        writer.write(content)
        rbuf.clear()
      finally
        writer.close()
    } match
      case scala.util.Success(_) =>
        println(s"Отчет сохранен: $filePath")
        Logging.info(s"Отчет сохранен: $filePath")
      case scala.util.Failure(e) =>
        Logging.error(s"Не удалось сохранить отчет: ${e.getMessage}")

    true