package ru.cyberc3dr.scalaapp.model

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.scala.DefaultScalaModule

import java.io.File
import scala.reflect.ClassTag

object JsonParser:
  private val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  def toJson(value: Any): String =
    mapper.writeValueAsString(value)

/*
  котлин гораздо лучше, потому что мы можем

  inline fun <reified T> fromJson(json: String) = mapper.readValue(json, T::class.java)

  но скала мне не дает это сделать нормально, поэтому так

  а еще в котлине есть kotlinx serialization, а тут нет
 */

  inline def fromJson[T](json: String)(using ct: ClassTag[T]) : T =
    mapper.readValue(json, ct.runtimeClass) match
      case result: T => result

  inline def fromFile[T](filePath: String)(using ct: ClassTag[T]) : T =
    val file = File(filePath)
    if !file.exists() then throw IllegalArgumentException(s"Файл по пути '$filePath' не найден.")
    mapper.readValue(file, ct.runtimeClass) match
      case result: T => result