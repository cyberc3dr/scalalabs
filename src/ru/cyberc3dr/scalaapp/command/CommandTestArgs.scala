package ru.cyberc3dr.scalaapp.command

object CommandTestArgs extends Command:
  override val name: String = "testargs"
  override val usage: String = "testargs <int> <word> (remaining)"

  override def execute(ctx: CommandContext): Boolean =
    val buf = ctx.buf
    
    val number = buf.getInt
    println(s"Введено целое число $number")
    
    val word = buf.getString(1)
    println(s"Получено слово $word")
    
    val remaining = buf.getString
    println(s"Оставшееся: $remaining")
    true
  
end CommandTestArgs

