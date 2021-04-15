open class Comment(
    val id      : Int,     //идентификатор комментария
    val uid     : Int = 0, //идентификатор автора комментария;
    val nid     : Int,     //идентификатор заметки. положительное число, обязательный параметр
    val oid     : Int = 0, //идентификатор владельца заметки положительное число, по умолчанию идентификатор
    // текущего пользователя
    val date    : Int,     // дата добавления комментария в формате unix time
    val replyTo : Int = 0, //идентификатор пользователя, ответом на комментарий которого является
    // добавляемый комментарий (не передаётся, если комментарий не является ответом).
    val message : String,  //текст комментария. строка, обязательный параметр
)
{
    fun copy(id      : Int = this.id,
             uid     : Int = this.uid,
             nid     : Int = this.nid,
             oid     : Int = this.oid,
             date    : Int = this.date,
             replyTo : Int = this.replyTo,
             message : String = this.message
    ) = Comment(id = id, uid = uid, nid = nid, oid = oid, date = date, replyTo = replyTo, message = message)
}