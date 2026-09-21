package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.CommandRegistry
import ru.cyberc3dr.scalaapp.model.{AppConfig, ConfigValidator, JsonParser}
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, HistoryManager}

import scala.util.{Failure, Success, Try}

object App:

  var config: Option[AppConfig] = None

  def main(args: Array[String]): Unit =
    EnvironmentChecker.check()

    reloadConfiguration()

    println(config)

    CommandRegistry.registerDefaults()
    InputHandler.start()

  def reloadConfiguration(): Unit =
    config = Try(JsonParser.fromFile[AppConfig]("config.json")) match
      case Success(value) =>
        val validation = ConfigValidator.validateConfig(value)
        if validation.isValid then
          Some(value)
        else
          println("Ошибки конфигурации:")
          validation.errors.foreach(err => println(s"  - $err"))
          None
      case Failure(e) =>
        println("Конфигурация не загружена! Проверьте синтаксис.")
        e.printStackTrace()
        None

    if config.isDefined then HistoryManager.load()

end App
