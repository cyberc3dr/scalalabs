package ru.cyberc3dr.scalaapp

import ru.cyberc3dr.scalaapp.command.{CommandRegistry, ReportBuffer}
import ru.cyberc3dr.scalaapp.errors.{AppError, ConfigInvalid}
import ru.cyberc3dr.scalaapp.model.{AppConfig, ConfigValidator, JsonParser}
import ru.cyberc3dr.scalaapp.ui.{CLI, MenuLoop, PredefinedUI, UI}
import ru.cyberc3dr.scalaapp.utils.{EnvironmentChecker, HistoryManager, Logging}

import scala.sys.addShutdownHook

// отступы вместо фигурных скобок это забавно
object App:

  // как же убого
  // lateinit var config: AppConfig
  // UPD: хотя option даже удобен в этом кейсе
  // Первый Option в этой программе.
  var config: Option[AppConfig] = None
  var reportBuffer: ReportBuffer = ReportBuffer()

  def main(args: Array[String]): Unit =
    reloadConfiguration()

    config.foreach(Logging.init)

    addShutdownHook {
      Logging.info("Приложение завершено")
      Logging.close()
    }

    Logging.info("Приложение запущено")
    Logging.info(s"Платформа определена: ${System.getProperty("os.name").toLowerCase}")

    if !EnvironmentChecker.check(true) then
      return

    CommandRegistry.registerDefaults()
    
    // меню можно крутить на одной из реализаций UI:
    //  - CLI  — реальный терминал;
    //  - PredefinedUI — предопределённый ввод (аргументы запуска, для тестов и демо).
    val ui: UI =
      if args.nonEmpty then PredefinedUI(args.toSeq)
      else CLI
      
    MenuLoop(ui, reportBuffer).run()

  /** Загружает и проверяет конфигурацию */
  def reloadConfiguration(): Either[AppError, AppConfig] =
    Logging.info("Перезагрузка конфигурации...")
    loadConfiguration("config.json") match
      case Right(value) =>
        config = Some(value)
        HistoryManager.load()
        Right(value)
      case Left(error) =>
        config = None
        Logging.error(error.message)
        error match
          case ConfigInvalid(_, problems) => problems.foreach(p => Logging.error(s"  - $p"))
          case _ => ()
        Logging.warn("Конфигурация не загружена, история тоже не будет загружена")
        Left(error)

  private def loadConfiguration(path: String): Either[AppError, AppConfig] =
    JsonParser.fromFile[AppConfig](path) match
      // Jackson не даст подгрузить файл с неправильной структурой или отсутствующий файл,
      // остальное проверим в ConfigValidator
      case Left(error) => Left(error)
      case Right(value) =>
        val validation = ConfigValidator.validateConfig(value)
        if validation.isValid then
          Logging.info("Конфигурация загружена и проверена успешно")
          Right(value)
        else
          Left(ConfigInvalid(path, validation.errors))

end App
