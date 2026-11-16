package ru.cyberc3dr.scalaapp.errors

sealed trait AppError

/** Ввод не удалось разобрать в команду. */
sealed trait ParsingError extends AppError

/** Команда разобрана, но её выполнение не удалось. */
sealed trait ExecutionError extends AppError

/** Конфигурация отсутствует или не прошла проверку. */
sealed trait ConfigError extends AppError

case class UnknownCommand(name: String) extends AppError

// ParsingError
case class MissingArgument(argument: String) extends ParsingError
case class InvalidArgument(value: String, expected: String) extends ParsingError

// ConfigError
case class ConfigMalformed(path: String, reason: String) extends ConfigError
case class ConfigInvalid(path: String, problems: List[String]) extends ConfigError

// ExecutionError
/** Конфигурация не загружена на момент выполнения команды. */
case object ConfigNotLoaded extends ExecutionError
/** Перезагрузка конфигурации не удалась — причина уже отформатирована. */
case class ConfigReloadFailed(reason: String) extends ExecutionError
case class ProfileNotFound(name: String) extends ExecutionError
case class HistoryNotFound(id: Int) extends ExecutionError
case class UtilityUnavailable(utility: String) extends ExecutionError
case class UnknownUtility(utility: String) extends ExecutionError
case class PlatformNotSupported(os: String) extends ExecutionError
case class HostNotFound(host: String) extends ExecutionError
case object GatewayNotFound extends ExecutionError
case class OutputParseFailed(utility: String, target: String) extends ExecutionError
case object OperationTimeout extends ExecutionError
case class IoFailure(path: String, reason: String) extends ExecutionError
case object ReportEmpty extends ExecutionError

/** Непредвиденная ошибка — страховочная сетка на краю системы. */
case class Internal(reason: String) extends ExecutionError
