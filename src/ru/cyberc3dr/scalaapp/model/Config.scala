package ru.cyberc3dr.scalaapp.model

case class AppConfig(
  logFile: String,
  historyFile: String,
  reportDirectory: String,
  defaultProfile: String,
  profiles: Map[String, Profile]
)

case class Profile(
  description: String,
  gateway: String,
  externalIpTargets: List[String],
  dnsNames: List[String],
  httpTargets: List[String],
  pingCount: Int,
  timeoutMilliseconds: Int,
  maximumRouteHops: Int,
  thresholds: Thresholds
)

case class Thresholds(
  warningLatencyMilliseconds: Int,
  criticalLatencyMilliseconds: Int,
  warningPacketLossPercent: Int,
  criticalPacketLossPercent: Int,
  warningHttpTimeMilliseconds: Int
)