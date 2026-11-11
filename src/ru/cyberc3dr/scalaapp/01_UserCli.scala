package ru.cyberc3dr.scalaapp

// scalac 01_UserCli.scala
// scala UserCli [путь-к-users.tsv]
// Либо: scala-cli run 01_UserCli.scala --scala 2.13.16 -- [путь-к-users.tsv]
// Без аргумента используется users.tsv в текущем каталоге.
// Изменения сохраняются командами save и exit; EOF завершает работу без сохранения.
// Имена и email в командах — один токен; в диалоге допустимы пробелы.
// Табуляции и переводы строк внутри полей TSV не поддерживаются.
// delete пока отсутствует: это первое задание.
// Выполняйте задания 1–9 по порядку. Это рефакторинг работающей программы.
// Ошибки могут встречаться сразу в нескольких заданиях: это естественно.
// ADT состояния, State/IO и собственные классы типов вводить не требуется.

import UserCli.args

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.collection.convert.ImplicitConversions.`seq AsJavaList`
import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn.readLine
import scala.util.control.NonFatal

object UserCli extends App {
  case class User(
      id: Int,
      var name: String,
      var email: String
  )

  val storagePath: Path = Paths.get(args.headOption.getOrElse("users.csv"))

  // ------------------------------------------------------------
  // Уровень хранения данных
  // ------------------------------------------------------------

  def loadUsers(path: Path): ArrayBuffer[User] = {
    try {
      if (!Files.exists(path)) {
        val initial = ArrayBuffer(
          User(1, "Alice", "alice@example.com"),
          User(2, "Bob", ""),
          User(3, "Charlie", "charlie@example.com")
        )
        if (!saveUsers(path, initial)) System.exit(1)
        initial
      } else {
        val lines = Files.readAllLines(path, StandardCharsets.UTF_8)
        val result = ArrayBuffer.empty[User]

        var i = 0
        while (i < lines.size()) {
          val parts = lines.get(i).split("\\t", -1)

          if (parts.length != 3) {
            throw new RuntimeException("Broken users file")
          }

          val id = parts(0).toInt
          val name = parts(1)
          val email = parts(2)
          if (id <= 0 || name.trim.isEmpty || result.exists(_.id == id))
            throw new RuntimeException("Broken users file: invalid id or name")
          result += User(id, name, email)
          i += 1
        }

        result
      }
    } catch {
      case NonFatal(ex) =>
        println("Cannot load users: " + ex.getMessage)
        System.exit(1)
        ArrayBuffer.empty[User] // формально нужно компилятору; сюда не дойдём
    }
  }

  def saveUsers(path: Path, users: ArrayBuffer[User]): Boolean = {
    try {
      val lines = new java.util.ArrayList[String]()

      var i = 0
      while (i < users.length) {
        val user = users(i)
        lines.add(s"${user.id}\t${user.name}\t${user.email}")
        i += 1
      }

      Files.write(
        path,
        lines,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
      )
      true
    } catch {
      case NonFatal(ex) =>
        println("Cannot save users: " + ex.getMessage)
        false
    }
  }

  def findUser(users: ArrayBuffer[User], id: Int): User = {
    var found: User = null
    var i = 0

    while (i < users.length && found == null) {
      if (users(i).id == id) {
        found = users(i)
      }
      i += 1
    }

    found
  }

  def createUser(users: ArrayBuffer[User], name: String, email: String): Boolean = {
    if (name.trim == "") {
      println("Name must not be empty.")
      false
    } else {
      var duplicate: User = null
      var i = 0

      while (i < users.length && duplicate == null) {
        if (users(i).name.equalsIgnoreCase(name)) {
          duplicate = users(i)
        }
        i += 1
      }

      if (duplicate != null) {
        println("User '" + name + "' already exists with id " + duplicate.id + ".")
        false
      } else {
        var maxId = 0
        i = 0
        while (i < users.length) {
          if (users(i).id > maxId) {
            maxId = users(i).id
          }
          i += 1
        }

        users += User(maxId + 1, name, email)
        true
      }
    }
  }

  def renameUser(users: ArrayBuffer[User], id: Int, newName: String): Boolean = {
    val user = findUser(users, id)

    if (user == null) {
      println("User not found.")
      false
    } else if (newName.trim == "") {
      println("Name must not be empty.")
      false
    } else {
      user.name = newName
      true
    }
  }

  def readText(prompt: String): String = {
    print(prompt)
    val text = readLine()
    if (text == null) running = false
    text
  }

  def readUserId(): Int = {
    var id = 0
    var done = false
    while (running && !done) {
      val text = readText("User id: ")
      if (text != null) {
        try {
          id = text.toInt
          if (id <= 0) println("Id must be positive.") else done = true
        } catch {
          case _: NumberFormatException => println("Id must be an integer. Try again.")
        }
      }
    }
    id
  }

  def show(id: Int): Unit = {
    val user = findUser(users, id)
    if (user == null) println("User not found.")
    else {
      println("Id: " + user.id)
      println("Name: " + user.name)
      println("Email: " + (if (user.email == "") "not set" else user.email))
    }
  }

  def showEmail(id: Int): Unit = {
    val user = findUser(users, id)
    if (user == null) println("User not found.")
    else if (user.email == "") println("Email is not set.")
    else println(user.email)
  }

  def withId(text: String)(action: Int => Unit): Unit = {
    try {
      val id = text.toInt
      if (id <= 0) println("Id must be positive.") else action(id)
    } catch {
      case _: NumberFormatException => println("Id must be an integer.")
    }
  }

  val users = loadUsers(storagePath)
  var running = true
  println("User CLI. Type 'help' for commands.")
  while (running) {
    val input = readText("> ")
    if (input != null) {
      input.trim.split("\\s+").toList match {
        case "help" :: Nil =>
          println("Commands: list, show <id>, create <name> [email], rename <id> <new-name>, email <id>, delete <id>, save, exit")
          println("show, create, rename, email without arguments start a dialog.")
        case "list" :: Nil =>
          if (users.isEmpty) println("No users.")
          else {
            var i = 0
            while (i < users.length) {
              println(s"${users(i).id}: ${users(i).name}")
              i += 1
            }
          }
        case "show" :: id :: Nil => withId(id)(show)
        case "email" :: id :: Nil => withId(id)(showEmail)
        case "create" :: name :: Nil =>
          if (createUser(users, name, "")) println("User created.")
        case "create" :: name :: email :: Nil =>
          if (createUser(users, name, email)) println("User created.")
        case "rename" :: id :: name :: Nil => withId(id) { n =>
          if (renameUser(users, n, name)) println("User renamed.")
        }
        case "show" :: Nil =>
          val id = readUserId()
          if (running) show(id)
        case "email" :: Nil =>
          val id = readUserId()
          if (running) showEmail(id)
        case "create" :: Nil =>
          val name = readText("Name: ")
          if (running) {
            val email = readText("Email (empty = not set): ")
            if (running && validFields(name, email) && createUser(users, name, email)) println("User created.")
          }
        case "rename" :: Nil =>
          val id = readUserId()
          if (running) {
            val name = readText("New name: ")
            if (running && validFields(name, "") && renameUser(users, id, name)) println("User renamed.")
          }
        case "save" :: Nil =>
          if (saveUsers(storagePath, users)) println("Saved.")
        case "exit" :: Nil =>
          if (saveUsers(storagePath, users)) running = false
        case "delete" :: Nil =>
          val id = readUserId()
          if (delete(id)) println("User deleted successfully bro");
        case _ => println("Unknown command or wrong arguments.")
      }
    }
  }

  def validFields(name: String, email: String): Boolean = {
    if (name.contains("\t") || email.contains("\t")) {
      println("Fields must not contain tabs.")
      false
    } else true
  }

  def delete(id: Int): Boolean = {
    if(!users.exists(_.id == id)) {
      println("User doesn't exist in database")
      return false
    }

    users.removeIf(_.id == id)
    true
  }

        // ============================================================
        // ЗАДАНИЕ 1. Добавить команду удаления
        // ============================================================
        //
        // Добавьте команду:
        //
        //   delete <id>
        //
        // Требования:
        //   1. id должен быть целым числом;
        //   2. если пользователя нет, вывести понятное сообщение;
        //   3. если пользователь найден, удалить его из users;
        //   4. после удаления вывести подтверждение;
        //   5. help тоже должен показывать новую команду.
        //
        // Выполните это задание В ТОМ ЖЕ СТИЛЕ, в котором уже написана
        // программа. Пока НЕ вводите ADT, Either, Option и map.
        //
        // После реализации ответьте себе на вопросы:
        //   - в скольких местах пришлось изменить программу?
        //   - какие куски логики пришлось повторить?
        //   - что именно в коде относится к синтаксису CLI, а что к самой
        //     операции удаления пользователя?
        //

  // ==================================================================
  // ЗАДАНИЕ 2. Отделить синтаксис команды от её смысла при помощи ADT
  // ==================================================================
  //
  // Сейчас после ввода строки программа сразу попадает в огромный match,
  // где одновременно:
  //   - распознаётся имя команды;
  //   - разбираются аргументы;
  //   - проверяется id;
  //   - выполняется операция;
  //   - печатается результат.
  //
  // Перепишите представление команд так, чтобы после разбора строки
  // остальная программа работала не со строками, а с типизированными
  // значениями.
  //
  // Создайте ADT Command. Например, в нём естественно иметь варианты:
  //   ListUsers
  //   ShowUser(id)
  //   CreateUser(name, email)
  //   RenameUser(id, newName)
  //   DeleteUser(id)
  //   ShowEmail(id)
  //   Exit
  //
  // ВАЖНО:
  //   * это НЕ требование скопировать перечисленные имена один в один;
  //   * смысл задания — после успешного разбора больше не таскать по
  //     программе исходную строку команды;
  //   * сначала определите типы, и только потом переписывайте код.
  //
  // После этого выделите отдельную функцию разбора строки.
  // На этом этапе допустимо вернуть Option[Command]:
  //
  //   def parseCommand(input: String): Option[Command]
  //
  // Пока не пытайтесь сообщать подробную причину каждой ошибки разбора.

  // ==================================================================
  // ЗАДАНИЕ 3. Коллекции обрабатываем как коллекции
  // ==================================================================
  //
  // После задания 2 найдите места, где List/Seq/ArrayBuffer обходятся
  // вручную только ради построения нового набора значений.
  //
  // Например, команда list должна сначала получить представление данных,
  // пригодное для отображения. Вместо ручного накопления результата
  // попробуйте выразить преобразование через map.
  //
  // Цель: научиться смотреть на задачу так:
  //
  //   List[User]
  //      + функция User => String
  //      = List[String]
  //
  // Не нужно заменять на map любой цикл вообще. Если цикл управляет
  // состоянием программы или делает несколько разных действий, обычный
  // цикл может оставаться понятнее.

  // ==================================================================
  // ЗАДАНИЕ 4. Нормальное отсутствие значения: Option
  // ==================================================================
  //
  // В исходной программе отсутствие значения кодируется двумя способами:
  //   - найденный пользователь хранится как User или null;
  //   - отсутствие email хранится как пустая строка.
  //
  // Измените модель так, чтобы:
  //   - поиск пользователя возвращал Option[User];
  //   - email пользователя имел тип Option[String].
  //
  // После этого реализуйте функции:
  //
  //   def findUserName(id: Int): Option[String]
  //   def findUserEmail(id: Int): Option[String]
  //
  // В первой функции естественно использовать map.
  // Во второй сначала попробуйте map и ПОСМОТРИТЕ НА ПОЛУЧИВШИЙСЯ ТИП.
  // Если получится Option[Option[String]], разберитесь, откуда взялись
  // два уровня Option, а затем получите Option[String] через flatMap.
  //
  // Не используйте .get.

  // ============================================================
  // ЗАДАНИЕ 5. Локальную ошибку не тащим до AppError
  // ============================================================
  //
  // Перепишите разбор числа так, чтобы NumberFormatException не был
  // основным способом описать ожидаемую ошибку пользователя.
  //
  // Выделите небольшую чистую функцию, например:
  //
  //   def parsePositiveInt(text: String): Option[Int]
  //
  // Она ничего не печатает и ничего не спрашивает повторно.
  // Она только сообщает: корректное положительное число есть или его нет.
  //
  // А readUserId() по-прежнему отвечает за диалог:
  //   - получил Some(id) -> возвращает id;
  //   - получил None     -> объясняет ошибку и спрашивает ещё раз.
  //
  // ВАЖНАЯ МЫСЛЬ:
  //   ошибка ввода здесь полностью разрешается на уровне CLI.
  //   Не нужно превращать её в AppError только потому, что у нас появился
  //   тип для ошибок.
  //

  // ============================================================
  // ЗАДАНИЕ 6. Ошибки операции как ADT
  // ============================================================
  //
  // Boolean из createUser/renameUser говорит только "получилось / нет",
  // но не сообщает вызывающему коду ПРИЧИНУ.
  // При этом функции сами печатают сообщения, то есть решают за CLI,
  // как именно реагировать на проблему.
  //
  // Создайте ADT ошибок операций над пользователями. Например, вам точно
  // понадобятся разные варианты для ситуаций:
  //   - пользователь с таким id не найден;
  //   - имя пустое;
  //   - пользователь с таким именем уже существует.
  //
  // После этого createUser/renameUser должны:
  //   - ничего не читать из консоли;
  //   - ничего не печатать;
  //   - возвращать Either[UserError, ...].
  //
  // Отдельно обратите внимание на UserAlreadyExists.
  // CLI МОЖЕТ обработать эту ошибку на месте. Например:
  //   - сообщить, какой пользователь уже существует;
  //   - спросить, хочет ли пользователь выбрать другое имя;
  //   - повторить только нужную часть диалога.
  //
  // После такой обработки UserAlreadyExists НЕ ОБЯЗАНА подниматься дальше.
  //
  // Сформулируйте правило своими словами:
  //   "ошибка поднимается выше только тогда, когда текущий уровень ..."
  //

  // ============================================================
  // ЗАДАНИЕ 7. Ошибки хранения — отдельный уровень
  // ============================================================
  //
  // Сейчас loadUsers/saveUsers сами:
  //   - ловят исключения;
  //   - печатают текст;
  //   - в одном случае вообще завершают процесс через System.exit.
  //
  // Выделите ADT StorageError и измените функции хранения так, чтобы их
  // публичный контракт был типизированным, например:
  //
  //   loadUsers(...): Either[StorageError, List[User]]
  //   saveUsers(...): Either[StorageError, Unit]
  //
  // Исключения Java API разрешается ловить ВНУТРИ слоя хранения и
  // превращать в StorageError.
  //
  // Важно различать:
  //   "мы используем exceptions внутри интеграции с Java API"
  // и
  //   "exceptions являются интерфейсом нашей бизнес-функции".
  //
  // Первое нормально. От второго в ожидаемых сценариях мы уходим.
  //
  // После этого решите отдельно для load и save:
  //   - может ли CLI осмысленно исправить ошибку здесь и сейчас?
  //   - если да, обработайте её здесь;
  //   - если нет, передайте на уровень приложения.
  //

  // ============================================================
  // ЗАДАНИЕ 8. AppError нужен не для каждой ошибки
  // ============================================================
  //
  // Создайте AppError только для тех ошибок, которые действительно должны
  // покинуть более узкий слой и стать проблемой запуска/работы приложения.
  //
  // Например, если StorageError невозможно разрешить на уровне CLI,
  // её можно преобразовать в AppError:
  //
  //   Either[StorageError, A]
  //              ↓
  //   Either[AppError, A]
  //
  // Для преобразования ошибки используйте преобразование внутри Either,
  // а не новый try/catch вокруг всей программы.
  //
  // Сравните три ситуации:
  //
  //   1. "abc" вместо id
  //      -> исправляется прямо при вводе;
  //
  //   2. имя уже занято
  //      -> CLI может предложить другое имя;
  //
  //   3. файл пользователей невозможно прочитать
  //      -> возможно, это уже AppError.
  //
  // Ошибка не обязана доходить до main.
  // Она должна подниматься только до уровня, который способен принять
  // осмысленное решение о том, что делать дальше.

  // ============================================================
  // ЗАДАНИЕ 9. map / flatMap вместо ручной распаковки контейнеров
  // ============================================================
  //
  // После перехода на Option/Either найдите код вида:
  //
  //   x match {
  //     case Some(value) => Some(f(value))
  //     case None        => None
  //   }
  //
  // или аналогичный код для Either.
  //
  // Если обе ветви отличаются только тем, что в успешной ветке значение
  // преобразуется, замените ручную распаковку на map.
  //
  // Если следующий шаг сам возвращает Option/Either, проверьте, не нужен
  // ли flatMap, чтобы не получить вложенный контейнер.
  //
  // Аналогично посмотрите на коллекции: если вы вручную проходите List
  // только для того, чтобы построить новый List, вероятно, это map/filter/
  // flatMap.
  //
  // Цель не в том, чтобы убрать все match и все циклы. Цель — не делать
  // вручную ту работу, которую уже выражает операция над контейнером.
}
