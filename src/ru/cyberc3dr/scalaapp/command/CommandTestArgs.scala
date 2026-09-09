package ru.cyberc3dr.scalaapp.command

object CommandTestArgs extends Command:
  override val name: String = "testargs"

  override def execute(ctx: CommandContext): Unit =
    val buf = ctx.buf
    
    val number = buf.getInt
    println(s"Введено целое число $number")
    
    val word = buf.getString(1)
    println(s"Получено слово $word")
    
    val remaining = buf.getString
    println(s"Оставшееся: $remaining")
  
end CommandTestArgs

