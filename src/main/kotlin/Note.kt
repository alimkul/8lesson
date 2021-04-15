open class Note (
    val id             : Int,             //идентификатор заметки.
    val ownerId        : Int     = 0,     //идентификатор пользователя
    val comments       : Int     = 0,     //количество комментариев
    val date           : Int     = 0,     //дата создания
    val title          : String  = "",    //заголовок заметки. строка, обязательный параметр
    val text           : String  = "",    //текст заметки. строка, обязательный параметр
    val canComment     : Boolean = false, //разрешено ли комментировать заметку
    val viewUrl        : String  = ""     //ссылка на заметку
){
    fun copy(id             : Int     = this.id ,
             ownerId        : Int     = this.ownerId,
             comments       : Int     = this.comments,
             date           : Int     = this.date,
             title          : String  = this.title,
             text           : String  = this.text,
             canComment     : Boolean = this.canComment,
             viewUrl        : String  = this.viewUrl    ) : Note{
        return Note(
            id=id,
            ownerId=ownerId,
            comments=comments,
            date=date,
            title=title,
            text=text,
            canComment=canComment,
            viewUrl=viewUrl
        )
    }
}