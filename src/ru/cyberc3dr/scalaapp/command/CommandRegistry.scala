package ru.cyberc3dr.scalaapp.command

import scala.collection.mutable.ListBuffer

// Новая версия регистра, теперь с объектами
object CommandRegistry:

  private val commands = ListBuffer.empty[CommandSpec]

  def register(spec: CommandSpec): Unit = commands += spec

  def registerDefaults(): Unit =
    register(CommandSpec(
      name = "exit",
      usage = "exit",
      description = "Выключение приложения",
      parser = ExitParser,
      executor = ExitExecutor
    ))
    register(CommandSpec(
      name = "testargs",
      usage = "testargs <int> <word> (remaining)",
      description = "Тест разбора аргументов командной строки",
      parser = TestArgsParser,
      executor = TestArgsExecutor
    ))
    register(CommandSpec(
      name = "profiles",
      usage = "profiles",
      description = "Список профилей из конфигурации",
      aliases = Seq("pr"),
      parser = ProfilesParser,
      executor = ProfilesExecutor
    ))
    register(CommandSpec(
      name = "reload",
      usage = "reload",
      description = "Перезагрузка конфигурации приложения",
      aliases = Seq("reloadconfig", "rlconfig"),
      parser = ReloadParser,
      executor = ReloadExecutor
    ))
    register(CommandSpec(
      name = "ping",
      usage = "ping <адрес> (количество пингов)",
      description = "Пинг узла по адресу",
      parser = PingParser,
      executor = PingExecutor
    ))
    register(CommandSpec(
      name = "http",
      usage = "http <адрес>",
      description = "HTTP-запрос по адресу (curl)",
      parser = HttpParser,
      executor = HttpExecutor
    ))
    register(CommandSpec(
      name = "dns",
      usage = "dns <адрес>",
      description = "DNS-resolve адреса (dig)",
      parser = DnsParser,
      executor = DnsExecutor
    ))
    register(CommandSpec(
      name = "trace",
      usage = "trace <адрес> (макс. кол-во хопов)",
      description = "Трассировка маршрута до узла",
      parser = TraceParser,
      executor = TraceExecutor
    ))
    register(CommandSpec(
      name = "diagnose",
      usage = "diagnose <профиль>",
      description = "Полная диагностика по профилю с сохранением в историю",
      aliases = Seq("diag"),
      parser = DiagnoseParser,
      executor = DiagnoseExecutor
    ))
    register(CommandSpec(
      name = "history",
      usage = "history",
      description = "Список сохранённых диагностик",
      parser = HistoryParser,
      executor = HistoryExecutor
    ))
    register(CommandSpec(
      name = "compare",
      usage = "compare <id1> <id2>",
      description = "Сравнение двух диагностик из истории",
      parser = CompareParser,
      executor = CompareExecutor
    ))
    register(CommandSpec(
      name = "report",
      usage = "report [имя_файла]",
      description = "Выгрузка накопленных отчётов в файл",
      parser = ReportParser,
      executor = ReportExecutor
    ))
    register(CommandSpec(
      name = "help",
      usage = "help [команда]",
      description = "Список доступных команд или справка по конкретной команде",
      aliases = Seq("h", "?", "помощь"),
      parser = HelpParser,
      executor = HelpExecutor
    ))

  def getAll: Seq[CommandSpec] = commands.toSeq
  
  def findByName(name: String): Option[CommandSpec] =
    commands.find(_.matches(name))

end CommandRegistry
