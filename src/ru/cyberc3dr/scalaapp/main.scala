package ru.cyberc3dr.scalaapp

import scala.io.StdIn
import scala.language.postfixOps

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@main
def main(): Unit =
  //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
  // to see how IntelliJ IDEA suggests fixing it.
  (1 to 5).map(println)

  for (i <- 1 to 5) do
    //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
    // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
    println(s"i = $i")

@main
def hello() =
  val x = "sdfg"
  println("Hello world")

class Human(val age:Int, name:String) {
  //  val id:Int = ???
  //  def setAge(newAge:Int) =
  //    age = newAge
  override def toString: String = s"Human($age , $name)"
}

def sqr(x:Double):Double = x*x


case class Person(age:Int, name:String)

// (Double, Double) => Double
def fDef(x: Double, y: Double): Double = 2 * x * y
val fLambda:(Double, Double) => Double = (x:Double, y:Double) => 2 * x * y

val v1 = fDef(10, 15)
val v2 = fLambda(10, 15)

case class MenuItem(name:String, action:()=>Unit) extends MenuTree:
  override def toString: String = name
  override def onChoose(): String =
    action()
    ""

trait MenuTree:
  def onChoose():String

class MenuController(mt:MenuTree):
  var currentElement:MenuTree = mt
  def readCommand():Int = StdIn.readInt()
  def menuLoop():Unit =
    println(currentElement.onChoose())
    val next = readCommand()

    //pattern matching
    currentElement = currentElement match
      case container: MenuContainer =>
        container.content(next)
      case _ => currentElement

    menuLoop()


class MenuContainer(val name:String, val content:Seq[MenuTree]) extends MenuTree:




  override def toString: String = name
  override def onChoose(): String =
    content
      .zipWithIndex
      .map{ case (e, i) => s"\n $i) " + e}
      .reduce(_ + _)

//    var buf:String = ""
//    for (i <- content.indices)
//      buf = buf + s"\n $i) " + content(i)
//    buf


@main
def classDemo() =
  val h = new Human(19, "Alice")
  val p = Person(20, "Bob")

  println(h)
  println(p)
  println(sqr(15))

  val x = if(0<1) 50 else 100
  val seq:Seq[Int] = Seq(1,2,5,6,16,8,16)
  // каждый элемент множить на 2
  val seq2 = seq
    .map(x => x * 3)
    .filter(x=> x % 2 == 0)
    .map(x=> x-4)

  println(seq2)

  def sum(seq:Seq[Int]):Int =
    var a = 0
    for (e <- seq) a += e
    a

  for (i <- seq)
    println(i)

@main
def menuDemo() =
  var x = 10
  val menu = MenuContainer("root",
    Seq(
      MenuItem("one",() => {
        x -= 5
        println (s"you have chosen poorly, x = $x")
      }),
      MenuItem("two", () => {
        x += 5
        println(s"you have chosen wisely, x = $x")
      }),
      MenuContainer("subMenu", Seq(
          MenuItem("one",()=>()),
          MenuItem("two",()=>()),
          MenuItem("three",()=>())
        ))
    )
  )
  val controller = MenuController(menu)
  controller.menuLoop()






