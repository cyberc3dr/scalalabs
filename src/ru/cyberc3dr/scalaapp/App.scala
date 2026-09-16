package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.CommandRegistry
import ru.cyberc3dr.scalaapp.model.{AppConfig, JsonParser}
import ru.cyberc3dr.scalaapp.utils.EnvironmentChecker

// отступы вместо фигурных скобок это забавно
object App:

  // как же убого
  // lateinit var config: AppConfig
  var config: Option[AppConfig] = None

  def main(args: Array[String]): Unit =
    EnvironmentChecker.check()

    reloadConfiguration()

    println(config)

    CommandRegistry.registerDefaults()
    InputHandler.start()

  def reloadConfiguration(): Unit =
    config = Option(JsonParser.fromFile[AppConfig]("config.json"))

end App
