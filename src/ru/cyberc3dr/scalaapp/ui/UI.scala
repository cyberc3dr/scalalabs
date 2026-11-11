package ru.cyberc3dr.scalaapp.ui

import ru.cyberc3dr.scalaapp.utils.Logging
import scala.io.StdIn

trait UI:
  /** Читает строку ввода; None — ввод закончился. */
  def readLine(): Option[String]

  /** Вывод результата команды. */
  def writeLine(text: String): Unit

  /** Записать ошибку на экран и в журнал. */
  def writeError(text: String): Unit

/** Реальный CLI. */
object CLI extends UI:

  def readLine(): Option[String] = Option(StdIn.readLine())

  def writeLine(text: String): Unit = println(text)

  def writeError(text: String): Unit = Logging.error(text)

/** Интерфейс с предопределённым вводом */
final class PredefinedUI(input: Seq[String]) extends UI:
  private val lines = input.iterator

  def readLine(): Option[String] =
    if lines.hasNext then Some(lines.next()) else None

  def writeLine(text: String): Unit = println(text)

  def writeError(text: String): Unit = Logging.error(text)

