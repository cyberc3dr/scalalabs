package ru.cyberc3dr.scalaapp.errors

extension (error: ExecutionError)
  def format: String = error match
    case ConfigNotLoaded => "Конфигурация не загружена. Попробуйте перезагрузить."
    case ConfigReloadFailed(reason) => s"Не удалось перезагрузить конфигурацию:\n$reason"
    case ProfileNotFound(name) => s"Профиль с именем '$name' не найден"
    case HistoryNotFound(id) => s"Диагностика #$id не найдена"
    case UtilityUnavailable(utility) => s"Утилита '$utility' недоступна"
    case UnknownUtility(utility) => s"Утилита '$utility' не входила в проверку окружения"
    case PlatformNotSupported(os) => s"Ошибка: Это приложение работает только на macOS (обнаружена система: $os)"
    case HostNotFound(host) => s"Хост '$host' не существует"
    case GatewayNotFound => "Не удалось обнаружить шлюз - вы не подключены к сети или находитесь под VPN"
    case OutputParseFailed(utility, target) => s"Не удалось разобрать вывод утилиты '$utility' (цель: $target)"
    case OperationTimeout => "Превышено время ожидания операции"
    case IoFailure(path, reason) => s"Ошибка ввода-вывода ('$path'): $reason"
    case ReportEmpty => "Отчет пуст - нет выполненных сетевых команд"
    case Internal(reason) => s"Внутренняя ошибка: $reason"

extension (error: ConfigError)
  def format: String = error match
    case ConfigMalformed(path, reason) => s"Конфигурация '$path' не загружена: $reason"
    case ConfigInvalid(_, problems) => s"Конфигурация содержит ошибки:\n" + problems.map(p => s"  - $p").mkString("\n")
