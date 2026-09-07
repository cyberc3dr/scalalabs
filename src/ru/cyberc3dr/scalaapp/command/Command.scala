package ru.cyberc3dr.scalaapp.command

// trait = interface, окей, живу с этим
trait Command:
  val name: String
  def execute(buf: StrBuffer): Unit
