package ru.cyberc3dr.scalaapp.command

// trait = interface, окей, живу с этим
trait Command:
  val name: String
  val usage: String = name
  val aliases: Seq[String] = Seq.empty[String]
  def execute(ctx: CommandContext): Boolean
