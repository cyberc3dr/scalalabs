package ru.cyberc3dr.scalaapp.model

import java.net.{InetAddress, URI}

object ConfigValidator:

  private def isValidIpAddress(address: String): Boolean =
    if address == "auto" then true
    else
      try
        val inetAddress = InetAddress.getByName(address)
        !inetAddress.isLoopbackAddress && !inetAddress.isAnyLocalAddress
      catch
        case _: Exception => false

  private def isValidUrl(url: String): Boolean =
    try
      val uri = URI(url)
      uri.getScheme != null && (uri.getScheme == "http" || uri.getScheme == "https") && uri.getHost != null
    catch
      case _: Exception => false

  private def isValidPath(path: String): Boolean =
    path != null && path.nonEmpty && path.trim.nonEmpty

  def validateConfig(config: AppConfig): ValidationResult =
    val errors = collection.mutable.ListBuffer[String]()

    if !isValidPath(config.logFile) then
      errors += "Путь к журналу не указан"

    if !isValidPath(config.historyFile) then
      errors += "Путь к истории не указан"

    if !isValidPath(config.reportDirectory) then
      errors += "Путь к отчётам не указан"

    if config.defaultProfile == null || config.defaultProfile.trim.isEmpty then
      errors += "Отсутствует профиль по умолчанию"
    else if !config.profiles.contains(config.defaultProfile) then
      errors += s"Профиль по умолчанию '${config.defaultProfile}' не существует"

    config.profiles.foreach { case (name, profile) =>
      errors ++= validateProfile(name, profile)
    }

    ValidationResult(errors.toList)

  private def validateProfile(profileName: String, profile: Profile): List[String] =
    val errors = collection.mutable.ListBuffer[String]()

    if profile.externalIpTargets == null || profile.externalIpTargets.isEmpty then
      errors += s"Профиль '$profileName': не задано ни одной внешней цели"

    if profile.gateway == null || profile.gateway.trim.isEmpty then
      errors += s"Профиль '$profileName': адрес шлюза не указан"
    else if !isValidIpAddress(profile.gateway) then
      errors += s"Профиль '$profileName': адрес шлюза имеет неправильный формат"

    if profile.pingCount < 1 then
      errors += s"Профиль '$profileName': количество пакетов меньше единицы"

    if profile.timeoutMilliseconds < 1 then
      errors += s"Профиль '$profileName': время ожидания меньше единицы"

    if profile.maximumRouteHops < 1 then
      errors += s"Профиль '$profileName': максимальное количество переходов меньше единицы"

    val thresholds = profile.thresholds
    if thresholds.warningLatencyMilliseconds > thresholds.criticalLatencyMilliseconds then
      errors += s"Профиль '$profileName': порог предупреждения по задержке превышает критический порог"

    if thresholds.warningPacketLossPercent > thresholds.criticalPacketLossPercent then
      errors += s"Профиль '$profileName': порог предупреждения по потерям превышает критический порог"

    if thresholds.warningPacketLossPercent < 0 || thresholds.warningPacketLossPercent > 100 then
      errors += s"Профиль '$profileName': процент потерь для предупреждения находится вне диапазона от 0 до 100"

    if thresholds.criticalPacketLossPercent < 0 || thresholds.criticalPacketLossPercent > 100 then
      errors += s"Профиль '$profileName': критический процент потерь находится вне диапазона от 0 до 100"

    if profile.httpTargets != null then
      profile.httpTargets.foreach { url =>
        if !isValidUrl(url) then
          errors += s"Профиль '$profileName': HTTP-адрес '$url' имеет неправильный формат"
      }

    errors.toList

  def validateProfileExists(config: AppConfig, profileName: String): Option[String] =
    if !config.profiles.contains(profileName) then
      Some(s"Профиль с именем '$profileName' не существует")
    else
      None

case class ValidationResult(errors: List[String]):
  def isValid: Boolean = errors.isEmpty
  def errorMessage: String = errors.mkString("\n")