package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.utils.HistoryManager

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CommandHistory extends Command:
  override val name: String = "history"
  override val usage: String = "history"

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())

  override def execute(ctx: CommandContext): Boolean =
    val entries = HistoryManager.getAll
    if entries.isEmpty then
      println("История пуста.")
      return true

    println("Сохранённые диагностики:")
    entries.foreach { e =>
      val state = e.result.state.name
      println(s"  ${e.id}   ${formatter.format(e.time)}  профиль=${e.profile}  состояние=$state")
    }
    true
