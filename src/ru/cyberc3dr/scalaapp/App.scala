package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.CommandRegistry
import ru.cyberc3dr.scalaapp.model.{AppConfig, JsonParser}
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, HistoryManager}

import scala.util.{Failure, Success, Try}

// отступы вместо фигурных скобок это забавно
object App:

  // как же убого
  // lateinit var config: AppConfig
  // UPD: хотя option даже удобен в этом кейсе
  var config: Option[AppConfig] = None

  def main(args: Array[String]): Unit =
    EnvironmentChecker.check()

    reloadConfiguration()

    println(config)

    CommandRegistry.registerDefaults()
    InputHandler.start()

  def reloadConfiguration(): Unit =
    config = Try(JsonParser.fromFile[AppConfig]("config.json")) match
      case Success(value) => Some(value)
      // Jackson не даст подгрузить файл с неправильной структурой, а на остальное все равно
      // У доменов, айпи адресов и так далее - нет четкой структуры, поэтому я не смогу ее проверить
      case Failure(e) => println("Конфигурация не загружена! Проверьте синтаксис."); None
      
    if config.isDefined then HistoryManager.load()

end App
