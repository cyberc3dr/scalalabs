package ru.cyberc3dr.scalaapp

import scala.io.StdIn

object App:

  trait MenuNode:
    val name: String
    def execute(): Unit

  case class MenuLeaf(override val name: String, func: () => Unit) extends MenuNode:
    override def execute(): Unit = func()

  case class MenuContainer(override val name: String, child: Seq[MenuNode]) extends MenuNode:
    def readInt(): Int = StdIn.readInt()

    def printNode(): Unit =
      child.zipWithIndex
        .foreach { case(node, i) =>
          println(s"${i+1}. ${node.name}")
        }

    override def execute(): Unit =
      var loop = true
      while loop do
        println("Menu: " + name)
        printNode()

        val chosen = readInt()

        if chosen == -1 then
          loop = false
        else if chosen - 1 >= 0 && chosen - 1 < child.length then
          val node = child(chosen - 1)
          node.execute()
        else
          println("Вы ввели неправильный номер")

  def main(args: Array[String]): Unit =
    println("Hello world!")

    val menu = MenuContainer("test", Seq(
      MenuLeaf("hello1", () => println("1111")),
      MenuLeaf("hello2", () => println("234678")),
      MenuLeaf("hello3", () => println("54215")),
      MenuLeaf("hello4", () => println("67677654")),
      MenuContainer("menu5", Seq(
        MenuLeaf("hello1", () => println("1111")),
        MenuLeaf("hello2", () => println("234678")),
        MenuLeaf("hello3", () => println("54215")),
      ))
    ))

    menu.execute()

end App
