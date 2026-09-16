package ru.cyberc3dr.scalaapp.net

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import ru.cyberc3dr.scalaapp.model.JsonParser
import ru.cyberc3dr.scalaapp.utils.EnvironmentChecker

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

def curl(address: String): CurlResult =
  if !EnvironmentChecker.isAvailable("curl") then
    throw IllegalStateException("Команда curl недоступна.")

  val result = executeCommand(s"curl -sL -o /dev/null -w \"%{json}\" $address")
  val json = result.head

  JsonParser.fromJson[CurlResult](json)