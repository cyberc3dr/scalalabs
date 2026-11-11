package ru.cyberc3dr.scalaapp.net

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import ru.cyberc3dr.scalaapp.errors.{AppError, OutputParseFailed, UtilityUnavailable}
import ru.cyberc3dr.scalaapp.model.JsonParser
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, Logging}

@JsonIgnoreProperties(ignoreUnknown = true) // Нам нужны не все поля
case class CurlResult(
  @JsonProperty("url") url: String,
  @JsonProperty("time_total") timeTotal: Double,
  @JsonProperty("http_code") httpCode: Int,
  @JsonProperty("num_redirects") redirects: Int,
  @JsonProperty("url_effective") urlEffective: String,
  @JsonProperty("ssl_verify_result") sslVerifyResult: Int
) {
  def timeTotalMs: Double = timeTotal * 1000
  def isAvailable: Boolean = httpCode > 0
  def hasTlsError: Boolean = sslVerifyResult != 0
}

def curl(address: String): Either[AppError, CurlResult] =
  if !EnvironmentChecker.isAvailable("curl") then return Left(UtilityUnavailable("curl"))

  val result = executeCommand(s"curl -sL -o /dev/null -w \"%{json}\" $address", "curl", address)

  result.output.headOption match
    case None => Left(OutputParseFailed("curl", address))
    case Some(json) =>
      JsonParser.fromJson[CurlResult](json).left.map { _ =>
        Logging.error(s"Не удалось разобрать вывод curl: цель=$address, вывод=$json")
        OutputParseFailed("curl", address)
      }
