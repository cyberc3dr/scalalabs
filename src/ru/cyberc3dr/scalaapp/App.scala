package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.CommandRegistry

object App:

  def main(args: Array[String]): Unit =
    println("Hello world!")
    CommandRegistry.registerDefaults()
    InputHandler.start()

end App
