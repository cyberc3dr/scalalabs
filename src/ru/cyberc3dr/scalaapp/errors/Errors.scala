package ru.cyberc3dr.scalaapp.errors

/** Главная ошибка в приложении */
sealed trait AppError:
  def message: String

/** Ввод не удалось разобрать в команду. */
sealed trait ParsingError extends AppError

/** Команда разобрана, но её выполнение не удалось. */
sealed trait ExecutionError extends AppError

/** Конфигурация отсутствует или не прошла проверку. */
sealed trait ConfigError extends AppError

// ParsingError

case class UnknownCommand(name: String) extends ParsingError:
  def message: String = s"Неизвестная команда: $name. Введите help для списка команд."

case class MissingArgument(argument: String) extends ParsingError:
  def message: String = s"Пропущен аргумент: $argument"

case class InvalidArgument(value: String, expected: String) extends ParsingError:
  def message: String = s"Некорректный аргумент '$value' — ожидалось $expected"

// ConfigError

case object ConfigNotLoaded extends ConfigError:
  def message: String = "Конфигурация не загружена. Попробуйте перезагрузить."

case class ConfigMalformed(path: String, reason: String) extends ConfigError:
  def message: String = s"Конфигурация '$path' не загружена: $reason"

case class ConfigInvalid(path: String, problems: List[String]) extends ConfigError:
  def message: String =
    s"Конфигурация '$path' содержит ошибки:\n" + problems.map(p => s"  - $p").mkString("\n")

// ExecutionError

case class ProfileNotFound(name: String) extends ExecutionError:
  def message: String = s"Профиль с именем '$name' не найден"

case class HistoryNotFound(id: Int) extends ExecutionError:
  def message: String = s"Диагностика #$id не найдена"

case class UtilityUnavailable(utility: String) extends ExecutionError:
  def message: String = s"Утилита '$utility' недоступна"

case class HostNotFound(host: String) extends ExecutionError:
  def message: String = s"Хост '$host' не существует"

case object GatewayNotFound extends ExecutionError:
  def message: String =
    "Не удалось обнаружить шлюз - вы не подключены к сети или находитесь под VPN"

case class OutputParseFailed(utility: String, target: String) extends ExecutionError:
  def message: String = s"Не удалось разобрать вывод утилиты '$utility' (цель: $target)"

case object OperationTimeout extends ExecutionError:
  def message: String = "Превышено время ожидания операции"

case class IoFailure(path: String, reason: String) extends ExecutionError:
  def message: String = s"Ошибка ввода-вывода ('$path'): $reason"

case object ReportEmpty extends ExecutionError:
  def message: String = "Отчет пуст - нет выполненных сетевых команд"

/** Непредвиденная ошибка */
case class Internal(reason: String) extends ExecutionError:
  def message: String = s"Внутренняя ошибка: $reason"
