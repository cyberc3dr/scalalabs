package ru.cyberc3dr.scalaapp.command

import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.utils.{HistoryManager, Logging}
import ru.cyberc3dr.scalaapp.net.diag

import scala.util.{Failure, Success, Try}

object CommandDiagnose extends Command:
  override val name: String = "diagnose"
  override val usage: String = "diagnose <профиль>"
  override val aliases: Seq[String] = Seq("diag")

  override def execute(ctx: CommandContext): Boolean =
    // костыль продакшн представляет фильм
    // как сделать диагностику монолитно и в одном файле
    // ну я просто хз как по нормальному сделать

    val buf = ctx.buf

    if !buf.hasNext then return false

    val config = App.config match
      case Some(value) => value
      case None => Logging.error("Конфигурация не загружена. Попробуйте перезагрузить."); return true

    val profileName = buf.getString(1)
    val profile = config.profiles.get(profileName) match
      case Some(value) => value
      case None => Logging.error(s"Профиль с именем $profileName не найден."); return true

    val diagnostic = diag(profile)

    println(Logging.format(diagnostic))

    val id = Try(HistoryManager.save(diagnostic, profileName)) match
      case Success(value) => value
      case Failure(e) =>
        Logging.error(s"Не удалось сохранить в историю: ${e.getMessage}")
        return true

    Logging.info(s"Диагностика сохранена: id=$id")
    println(s"Диагностика сохранена: id=$id")

    true


