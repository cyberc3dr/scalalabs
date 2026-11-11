package ru.cyberc3dr.scalaapp.command

/** Команда разобранная из пользовательского ввода. */
sealed trait Command

final case class PingCommand(address: String, count: Int) extends Command
final case class DnsCommand(address: String) extends Command
final case class HttpCommand(address: String) extends Command
final case class TraceCommand(address: String, maxHops: Int) extends Command
final case class DiagnoseCommand(profileName: String) extends Command
final case class CompareCommand(firstId: Int, secondId: Int) extends Command
final case class ReportCommand(fileName: Option[String]) extends Command
final case class HelpCommand(query: Option[String]) extends Command
final case class TestArgsCommand(number: Int, word: String, remaining: String) extends Command

case object ProfilesCommand extends Command
case object HistoryCommand extends Command
case object ReloadCommand extends Command
case object ExitCommand extends Command
