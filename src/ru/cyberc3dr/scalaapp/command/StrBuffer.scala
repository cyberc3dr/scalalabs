package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.errors.{AppError, InvalidArgument, MissingArgument}

/** Курсор по словам введённой строки. */
class StrBuffer(private val array: Array[String]):

  private var index = 0

  def hasNext: Boolean = index < array.length

  def getInt: Either[AppError, Int] =
    if !hasNext then Left(MissingArgument("целое число"))
    else
      val str = array(index)
      index += 1
      str.toIntOption.toRight(InvalidArgument(str, "целое число"))

  /** Читает целое число, либо возвращает значение по умолчанию, если аргументов больше нет. */
  def getIntOrElse(default: Int): Either[AppError, Int] =
    if hasNext then getInt else Right(default)

  def getDouble: Either[AppError, Double] =
    if !hasNext then Left(MissingArgument("число"))
    else
      val str = array(index)
      index += 1
      str.toDoubleOption.toRight(InvalidArgument(str, "число"))

  /** Считывает count слов, склеивая их пробелами. */
  def getString(count: Int): Either[AppError, String] =
    if count < 1 then Left(InvalidArgument(count.toString, "положительное количество слов"))
    else if !hasNext then Left(MissingArgument("строка"))
    else
      val builder = StringBuilder()
      val end = math.min(index + count, array.length)

      while index < end do
        builder.append(array(index))
        index += 1
        if index < end then builder.append(" ")

      Right(builder.toString())

  /** Считывает все оставшиеся слова */
  def getRemaining: String =
    getString(array.length - index).getOrElse("")

end StrBuffer
