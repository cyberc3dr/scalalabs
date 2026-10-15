package ru.cyberc3dr.scalaapp.command

object CommandHelp extends Command:
  override val name: String = "help"
  override val usage: String = "help [команда]"
  override val description: String = "Список доступных команд или справка по конкретной команде"
  override val aliases: Seq[String] = Seq("h", "?", "помощь")

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf

    if !buf.hasNext then printAllCommands()
    else
      val query = buf.getString(1)
      CommandRegistry.findByName(query) match
        case Some(cmd) => printCommandHelp(cmd)
        case None =>
          println(s"Команда '$query' не найдена.")
          println("Доступные команды:")
          printAllCommands()

    true

  private def printAllCommands(): Unit =
    val commands = CommandRegistry.getAll.sortBy(_.name)
    val width = commands.map(_.name.length).maxOption.getOrElse(0)

    println(s"Доступные команды (${commands.size}):")
    println("-" * 60)
    commands.foreach { cmd =>
      val aliases = if cmd.aliases.isEmpty then "" else s" [алиасы: ${cmd.aliases.mkString(", ")}]"
      println(s" ${cmd.name.padTo(width, ' ')} - ${cmd.description}$aliases")
    }
    println("-" * 60)
    println("Подробнее о команде: help <команда>")

  private def printCommandHelp(cmd: Command): Unit =
    println(s"Команда: ${cmd.name}")
    if cmd.aliases.nonEmpty then println(s"Алиасы: ${cmd.aliases.mkString(", ")}")
    println(s"Использование: ${cmd.usage}")
    if cmd.description.nonEmpty then println(s"Описание: ${cmd.description}")

end CommandHelp