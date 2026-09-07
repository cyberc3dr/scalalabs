package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.CommandRegistry
import ru.cyberc3dr.scalaapp.model.{JsonParser, TestModel}

// отступы вместо фигурных скобок это забавно
object App:

  def main(args: Array[String]): Unit =
    println("Hello world!")

    val testModel = JsonParser.fromFile[TestModel]("test.json")

    println(testModel)

    CommandRegistry.registerDefaults()
    InputHandler.start()

end App
