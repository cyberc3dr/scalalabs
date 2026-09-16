package ru.cyberc3dr.scalaapp.net

import java.time.Instant

enum DiagnosticState:
  case NoInterface
  case GatewayUnavailable
  case NoInternet
  case DnsIssue
  case ResourceUnavailable
  case Unstable
  case Good

case class DiagnosticResult(
  time: Instant = Instant.now(),
  state: DiagnosticState,
  pings: Option[List[PingStats]],
  dns: Option[List[DnsStats]],
  https: Option[List[CurlResult]],
  trace: Option[TraceStats]
)