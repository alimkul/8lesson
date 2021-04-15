
import sun.awt.Mutex
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicInteger

class NotesWithComments(userId: Int, override val canComment: Boolean = true) : Notes(userId)
{
    private val mutex               = Mutex()
    private var idGenerator         = AtomicInteger()
    private fun generateId() : Int  = idGenerator.incrementAndGet()
    private var deletedComments     = sortedMapOf<Int,Comment>()
    private var comments            = sortedMapOf<Int,Comment>()
    override fun dropNoteComments(noteId : Int) {
        mutex.lock()
        try {
            comments        = comments       .filterNot {it.value.nid == noteId}.toSortedMap()
            deletedComments = deletedComments.filterNot {it.value.nid == noteId}.toSortedMap()
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     * Добавляет новый комментарий к заметке.
     * После успешного выполнения возвращает идентификатор созданного комментария (cid).
     */
    fun createComment( noteId  : Int, //идентификатор заметки. положительное число, обязательный параметр
                       ownerId : Int = userId, //идентификатор владельца заметки положительное число, по умолчанию идентификатор
        // текущего пользователя
                       replyTo : Int = 0, //идентификатор пользователя, ответом на комментарий которого является
        // добавляемый комментарий (не передаётся, если комментарий не является ответом).
                       message : String,  //текст комментария. строка, обязательный параметр
                       guid    : String   //уникальный идентификатор, предназначенный для предотвращения повторной
        // отправки одинакового комментария.
    ) : Int
    {
        mutex.lock()
        try {  //проверка по guid, что такоего еще не было
            val dupleComment = comments.values.find { (it as CommentData).guid == guid }
            if (dupleComment != null)
                throw CommentAlreadyExistsException(message, guid)

            notes[noteId].apply {
                this ?:               throw NoteNotFoundException(noteId)
                if (!this.canComment) throw ForbiddenToComment(noteId)
            }
            if (ownerId != userId)    throw WrongNoteAuthorException(ownerId, userId)

            generateId().apply {
                comments[this] = CommentData(
                    Comment(id = this, nid = noteId, oid = ownerId,   //дата = id для простоты
                        replyTo = replyTo, message = message, date = this), guid = guid)
                return this
            }
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     *Удаляет комментарий к заметке.
     * После успешного выполнения возвращает 1.
     */
    fun deleteComment(commentId : Int, ownerId : Int = userId) : Int
    {
        mutex.lock()
        try {
            if (ownerId != userId)  throw WrongNoteAuthorException(ownerId, userId)
            comments.remove(commentId).also {
                deletedComments[commentId] = it
            } ?: throw CommentNotFoundException(commentId)
            return 1
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     * Редактирует указанный комментарий у заметки.
     * После успешного выполнения возвращает 1.
     */
    fun editComment(commentId : Int, ownerId : Int = userId, message : String) : Int
    {
        mutex.lock()
        try {
            if (ownerId != userId)   throw WrongNoteAuthorException(ownerId, userId)
            comments[commentId].apply {
                this ?:              throw CommentNotFoundException(commentId)
                comments[commentId] = (this as CommentData).copy(this.copy(message = message))
            }
            return 1
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     * Возвращает список комментариев к заметке.
     * noteId -  идентификатор_заметки.
     * userId  - идентификатор пользователя, информацию о заметках которого требуется получить
     * offset  - смещение, необходимое для выборки определенного подмножества заметок
     * count   - количество заметок, информацию о которых необходимо получит
     * sort    - сортировка результатов (0 — по дате создания в порядке убывания, 1 - по дате создания в порядке возрастания).
     */
    fun getComments(noteId : Int, ownerId : Int = this.userId,
                    offset :Int = 0, count : Int = 20, sort : Boolean = false) : Array<Comment>
    {
        mutex.lock()
        try{
            if (ownerId != userId)  throw WrongNoteAuthorException(ownerId, userId)
            val fullList = if (sort) {
                comments.filter { noteId == it.value.nid }.flatMap { listOf(it.value) }.sortedBy { it.date }
            } else {
                comments.filter { noteId == it.value.nid }.flatMap { listOf(it.value) }.sortedByDescending { it.date }
            }
            //subList генерит исключения при неверном индексе
            return if (offset + count <= fullList.size)
            {
                fullList.subList(offset, offset + count).toTypedArray()
            } else {
                if (offset < fullList.size) {
                    fullList.subList(offset, fullList.size - offset).toTypedArray()
                }else arrayOf()
            }
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     * Восстанавливает удалённый комментарий.
     * После успешного выполнения возвращает 1
     */
    fun restoreComment(commentId : Int, ownerId : Int) : Int{
        mutex.lock()
        try {
            if (ownerId != userId)  throw WrongNoteAuthorException(ownerId, userId)
            deletedComments.remove(commentId).also {
                comments[commentId] = it
            } ?: throw CommentNotFoundException(commentId)
            return 1
        }
        finally {
            mutex.unlock()
        }
    }
}

class CommentData(
    comment  : Comment,
    val guid : String  //уникальный идентификатор, предназначенный для предотвращения повторной
    // отправки одинакового комментария.
) : Comment (
    id = comment.id,   uid = comment.uid,    nid = comment.nid,
    oid = comment.oid, date = comment.date, replyTo = comment.replyTo, message = comment.message
)
{
    fun copy(comment : Comment = this, guid : String = this.guid) =
        CommentData(comment, guid)
}

class ForbiddenToComment(noteId : Int) :
    RuntimeException("Forbidden To Comment Note with id=$noteId")

class CommentNotFoundException(commentId : Int) :
    RuntimeException("Comment with id=$commentId not found")

class CommentAlreadyExistsException(text : String, guid : String) :
    RuntimeException("Comment \"$text\" with guid \"$guid\" Already Exists")

class WrongNoteAuthorException(userId: Int, noteAuthorId: Int) :
    RuntimeException("Wrong Note Author $userId Author Id is $noteAuthorId")