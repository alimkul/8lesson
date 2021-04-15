
import  sun.awt.Mutex
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicInteger

open class Notes(val  userId: Int) {
    protected open val canComment   = false
    private val mutex               = Mutex()
    private var idGenerator         = AtomicInteger()
    private fun generateId() : Int  = idGenerator.incrementAndGet()
    protected var notes             = sortedMapOf<Int,NoteData>()
    //в данном классе функция ничего не делает. Будет переопределена в наследнике
    protected open fun dropNoteComments(noteId : Int) = Unit

    /**
     * Создает новую заметку у текущего пользователя.
     * После успешного выполнения возвращает идентификатор созданной заметки (nid).
     */
    fun add(title : String, text : String,
            privacyView    : Array<String> = emptyArray(),
            privacyComment : Array<String> = emptyArray()) : Int
    {
        val id = generateId()
        mutex.lock()  //id считаем как время, потому что и то и другое увеличивается с номером поста
        notes[id] = NoteData(note = Note(id = id, ownerId = userId, title = title, text = text,
            date = id, canComment = canComment),
            privacyView = privacyView, privacyComment = privacyComment)
        mutex.unlock()
        return id
    }

    /**
     * Удаляет заметку текущего пользователя.
     * Возваращает 1 в случае успеха
     */
    fun delete(noteId : Int) : Int
    {
        //удаляем безвозвратно комментарии на заметку
        dropNoteComments(noteId)
        mutex.lock()
        try {
            //удаляем саму заметку
            notes.remove(noteId) ?: throw NoteNotFoundException(noteId)
            return 1
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     * Редактирует заметку текущего пользователя.
     * Возваращает 1 в случае успеха
     */
    fun edit(noteId : Int, title : String, text : String,
             privacyView    : Array<String> = emptyArray(),
             privacyComment : Array<String> = emptyArray()) : Int
    {
        mutex.lock()
        try {
            val note = notes[noteId] ?: throw NoteNotFoundException(noteId)
            if (note.checkPrivacy(privacyView = privacyView, privacyComment = privacyComment))
            {
                notes[noteId] = note.copy(note.copy(title = title, text = text))
            }
            else throw InsufficientPermissionsForEdit(noteId)
        }
        finally {
            mutex.unlock()
        }
        return 1
    }

    /**
     * Возвращает список заметок, созданных пользователем.
     * noteIds - идентификаторы заметок, информацию о которых необходимо получить
     * userId  - идентификатор пользователя, информацию о заметках которого требуется получить
     * offset  - смещение, необходимое для выборки определенного подмножества заметок
     * count   - количество заметок, информацию о которых необходимо получить
     * sort    - сортировка результатов (0 — по дате создания в порядке убывания, 1 - по дате создания в порядке возрастания).
     */
    fun get(noteIds : Iterable<Int>, userId : Int = this.userId,
            offset :Int = 0, count : Int = 20, sort : Boolean = false) : List<Note>
    {
        mutex.lock()
        try{
            if (this.userId != userId)  throw WrongNoteAuthorException(userId, this.userId)
            val fullList = if (sort) {
                notes.filterKeys { noteIds.contains(it) }.flatMap { listOf(it.value) }.sortedBy { it.date }
            } else {
                notes.filterKeys { noteIds.contains(it) }.flatMap { listOf(it.value) }
                    .sortedByDescending { it.date }
            }
            //subList генерит исключения при неверном индексе
            return if (offset + count <= fullList.size)
            {
                fullList.subList(offset, offset + count)
            } else {
                if (offset < fullList.size) {
                    fullList.subList(offset, fullList.size - offset)
                }else listOf()
            }
        }
        finally {
            mutex.unlock()
        }
    }

    /**
     * Возвращает заметку по её id в виде списка ArrayList<GetIdNote>
     */
    fun getById(noteId : Int, ownerId : Int = this.userId, needWiki : Boolean = false) :
            ArrayList<GetIdNote>
    {
        mutex.lock()
        try {
            if (this.userId != ownerId)  throw WrongNoteAuthorException(ownerId, userId)
            return arrayListOf(GetIdNote(notes[noteId] ?: throw NoteNotFoundException(noteId)))
        }
        finally {
            mutex.unlock()
        }
    }
}

class NoteNotFoundException         (id : Int) : RuntimeException("Note with id=$id not found")
class InsufficientPermissionsForEdit(id : Int) : RuntimeException("Insufficient Permissions for edit Note $id")

class NoteData(
    note           : Note,
    val privacyView    : Array<String> = arrayOf("all"),
    val privacyComment : Array<String> = arrayOf("all")
) : Note(
    id = note.id,
    ownerId = note.ownerId,
    comments = note.comments,
    date = note.date,
    title = note.title,
    text = note.text,
    canComment = note.canComment,
    viewUrl = note.viewUrl
)
{
    fun checkPrivacy(privacyView : Array<String>, privacyComment : Array<String>) : Boolean
    {
        return this.privacyView    contentEquals privacyView    &&
                this.privacyComment contentEquals privacyComment
    }

    fun copy(note : Note = this,
             privacyView    : Array<String> = this.privacyComment,
             privacyComment : Array<String> = this.privacyComment): NoteData
    {
        return NoteData(note = note, privacyView = privacyView, privacyComment = privacyComment)
    }
}

class GetIdNote (
    note           : Note,
    val privacy        : Int = 0,
    val commentPrivacy : Int = 1
) : Note(
    id = note.id,
    ownerId = note.ownerId,
    comments = note.comments,
    date = note.date,
    title = note.title,
    text = note.text,
    canComment = note.canComment,
    viewUrl = note.viewUrl
)
