package ru.cyberc3dr.scalaapp.command

class ReportBuffer:
  private val builder = StringBuilder()

  def append(text: String): Unit =
    builder.append(text)

  def appendLine(text: String): Unit =
    builder.append(text).append("\n")

  def getContent: String = builder.toString()

  def clear(): Unit = builder.clear()

end ReportBuffer