package ru.cyberc3dr.scalaapp.utils

import ru.cyberc3dr.scalaapp.App
import ru.cyberc3dr.scalaapp.model.{HistoryEntry, JsonParser}
import ru.cyberc3dr.scalaapp.net.DiagnosticResult

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object HistoryManager:

  private val cache = ListBuffer.empty[HistoryEntry]

  private def filePath: String =
    App.config match
      case Some(cfg) => cfg.historyFile
      case None => throw IllegalStateException("Конфигурация не загружена")
  
  def load(): Unit =
    cache.clear()
    
    // нестираемое, какой же все таки ужасный инлайн в скале
    Try(JsonParser.fromFile[Array[HistoryEntry]](filePath)) match
      case Success(entries) => cache.addAll(entries)
      case Failure(_) => // повреждённый файл или нет файла — начинаем с пустой истории

  def save(result: DiagnosticResult, profileName: String): Int =
    // может ничего не быть в принципе
    val id = cache.map(_.id).maxOption.getOrElse(0) + 1
    val entry = HistoryEntry(id, java.time.Instant.now(), profileName, result)
    cache += entry
    
    JsonParser.saveToFile(cache.toList, filePath)
    id

  def getAll: List[HistoryEntry] = cache.toList
  def getById(id: Int): Option[HistoryEntry] = cache.find(_.id == id)
