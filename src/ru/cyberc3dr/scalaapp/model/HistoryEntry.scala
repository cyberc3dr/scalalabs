package ru.cyberc3dr.scalaapp.model

import ru.cyberc3dr.scalaapp.net.DiagnosticResult

import java.time.Instant

case class HistoryEntry(
  id: Int,
  time: Instant,
  profile: String,
  result: DiagnosticResult
)
