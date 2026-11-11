package ru.cyberc3dr.scalaapp.utils

import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.errors.{AppError, ConfigNotLoaded, IoFailure}
import ru.cyberc3dr.scalaapp.model.{HistoryEntry, JsonParser}
import ru.cyberc3dr.scalaapp.net.DiagnosticResult

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object HistoryManager:

  private val cache = ListBuffer.empty[HistoryEntry]

  private def filePath: Either[AppError, String] =
    App.config.toRight(ConfigNotLoaded).map(_.historyFile)

  def load(): Unit =
    cache.clear()

    App.config match
      case None =>
        Logging.warn("Конфигурация не загружена, история не может быть загружена")
      case Some(cfg) =>
        // повреждённый файл или нет файла — начинаем с пустой истории
        JsonParser.fromFile[Array[HistoryEntry]](cfg.historyFile) match
          case Right(entries) =>
            cache.addAll(entries)
            Logging.info(s"История загружена: записей=${entries.length}")
          case Left(error) =>
            Logging.warn(s"Не удалось загрузить историю (файл поврежден или отсутствует): ${error.message}")

  def save(result: DiagnosticResult, profileName: String): Either[AppError, Int] =
    filePath match
      case Left(error) => Left(error)
      case Right(path) =>
        val id = cache.map(_.id).maxOption.getOrElse(0) + 1
        val entry = HistoryEntry(id, java.time.Instant.now(), profileName, result)
        cache += entry

        Try(JsonParser.saveToFile(cache.toList, path)) match
          case Success(_) =>
            Logging.info(s"История сохранена: id=$id, профиль=$profileName")
            Right(id)
          case Failure(e) => Left(IoFailure(path, e.getMessage))

  def getAll: List[HistoryEntry] = cache.toList
  def getById(id: Int): Option[HistoryEntry] = cache.find(_.id == id)
