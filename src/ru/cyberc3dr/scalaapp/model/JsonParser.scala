package ru.cyberc3dr.scalaapp.model

import ru.cyberc3dr.scalaapp.errors.{AppError, ConfigMalformed, OutputParseFailed}
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.scala.DefaultScalaModule

import java.io.File
import scala.reflect.ClassTag
import scala.util.Try

object JsonParser:
  private val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  def toJson(value: Any): String =
    mapper.writeValueAsString(value)

  def saveToFile(value: Any, filePath: String): Unit =
    mapper.writeValue(new File(filePath), value)

/*
  котлин гораздо лучше, потому что мы можем

  inline fun <reified T> fromJson(json: String) = mapper.readValue(json, T::class.java)

  но скала мне не дает это сделать нормально, поэтому так

  а еще в котлине есть kotlinx serialization, а тут нет

  UPD: внутри Option/Some происходит стирание типа, используйте передачу через []
  UPD 2: внутри List происходит стирание типа, используйте Array
  TL;DR : инлайны в скале ужас, не используйте, потеряете кучу времени
    
  а, ну ладно
 */

  inline def fromJson[T](json: String)(using ct: ClassTag[T]): Either[AppError, T] =
    Try(mapper.readValue(json, ct.runtimeClass).asInstanceOf[T])
      .toEither.left.map(_ => OutputParseFailed("json", json.take(120)))

  inline def fromFile[T](filePath: String)(using ct: ClassTag[T]): Either[AppError, T] =
    val file = File(filePath)
    if !file.exists() then Left(ConfigMalformed(filePath, "файл не найден"))
    else
      Try(mapper.readValue(file, ct.runtimeClass).asInstanceOf[T])
        .toEither.left.map(e => ConfigMalformed(filePath, e.getMessage))
