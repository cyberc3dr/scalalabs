package ru.cyberc3dr.scalaapp.command

trait Command:
  val name: String
  def execute(buf: StrBuffer): Unit
