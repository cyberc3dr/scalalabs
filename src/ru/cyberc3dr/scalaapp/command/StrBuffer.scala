package ru.cyberc3dr.scalaapp.command

import java.util.NoSuchElementException

class StrBuffer(private val array: Array[String]):

  private var index = 0

  def hasNext: Boolean = index < array.length

  def getInt: Int =
    if !hasNext then throw NoSuchElementException("Попытка чтения за границами данных")

    val str = array(index)
    index += 1
    str.toIntOption match
      case Some(value) => value
      case None => throw IllegalArgumentException(s"Не удалось перевести $str в Int на позиции $index")

  def getDouble: Double =
    if !hasNext then throw NoSuchElementException("Попытка чтения за границами данных")

    val str = array(index)
    index += 1
    str.toDoubleOption match
      case Some(value) => value
      case None => throw IllegalArgumentException(s"Не удалось перевести $str в Double на позиции $index")

  def getString(count: Int): String =
    if count < 1 || !hasNext then return ""

    val builder = StringBuilder()
    val end = math.min(index + count, array.length)

    while index < end do
      builder.append(array(index))
      index += 1
      if index < end then builder.append(" ")

    builder.toString()

  def getString: String =
    val remainingWords = array.length - index
    getString(remainingWords)

end StrBuffer

