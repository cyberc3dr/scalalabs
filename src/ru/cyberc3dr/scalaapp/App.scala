package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.CommandRegistry
import ru.cyberc3dr.scalaapp.model.{AppConfig, JsonParser}

// отступы вместо фигурных скобок это забавно
object App:

  def main(args: Array[String]): Unit =
    println("Hello world!")

    val config: AppConfig = JsonParser.fromFile("example.json")

    println(config)

    CommandRegistry.registerDefaults()
    InputHandler.start()

end App
