package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.{CommandRegistry, ReportBuffer}
import ru.cyberc3dr.scalaapp.model.{AppConfig, ConfigValidator, JsonParser}
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, HistoryManager, Logging}

import scala.util.{Failure, Success, Try}
import scala.sys.addShutdownHook

object App:

  var config: Option[AppConfig] = None
  var reportBuffer: ReportBuffer = ReportBuffer()

  def main(args: Array[String]): Unit =
    reloadConfiguration()

    config.foreach(Logging.init)

    addShutdownHook {
      Logging.info("Приложение завершено")
      Logging.close()
    }

    Logging.info("Приложение запущено")
    Logging.info(s"Платформа определена: ${System.getProperty("os.name").toLowerCase}")

    if !EnvironmentChecker.check(true) then
      return

    CommandRegistry.registerDefaults()
    InputHandler.start(reportBuffer)

  def reloadConfiguration(): Unit =
    config = Try(JsonParser.fromFile[AppConfig]("config.json")) match
      case Success(value) =>
        val validation = ConfigValidator.validateConfig(value)
        if validation.isValid then
          Some(value)
        else
          Logging.error("Ошибки конфигурации:")
          validation.errors.foreach(err => Logging.error(s"  - $err"))
          None
      case Failure(e) =>
        Logging.error("Конфигурация не загружена! Проверьте синтаксис.")
        e.printStackTrace()
        None

    if config.isDefined then HistoryManager.load()

end App
