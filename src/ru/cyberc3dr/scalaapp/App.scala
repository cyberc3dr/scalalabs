package ru.cyberc3dr.scalaapp

object App:

  private type Str = String
  private type R = Double

  def main(args: Array[String]): Unit =
    val x: Str = ""
    val z = f(1)

    val func = (x: R) => x * x
    println("Квадрат: " + func(2))

    println((1 to 5).map(_ * 2))
    println((1 to 5).reduceLeft(_ + _))
    println((1 to 5).map(x => x * x))

    println(
      (1 to 5).map { x =>
        val y = x * 2
        println(y)
        y
      }
    )

    println(
      (1 to 5) filter {
        _ % 2 == 0
      } map {
        _ * 2
      }
    )

    def compose(g: R => R, h: R => R) =
      (x: R) => g(h(x))

    val f1 = compose(_ * 2, _ - 1)

    println(f1(2))

    val zscore =
      (mean: R, sd: R) =>
        (x: R) =>
          (x - mean) / sd

    println(zscore(1, 2)(2))

    val normer = zscore(7, 0.4) _

    println(normer(3))

    println(mapmake[Int](_ * 2)((1, 2, 3, 4, 5).toList))

    println((1 to 5) map (_ * 2))

    println(sum(1, 2, 3, 4, 5))

    1 :: List(2, 3)




  def f(x: => Int): R =
    x.toDouble

  def sum(args: Int*) =
    args.reduceLeft(_ + _)

  def mapmake[T](g: T => T)(seq: List[T]) =
    seq.map(g)

end App
