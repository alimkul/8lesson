

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesTest {

    @Test(expected = NoteNotFoundException::class)
    fun delete_fails() {
        val notes = Notes(1)

        notes.delete(1)
    }

    @Test
    fun delete_succeeded() {
        val notes = Notes(1)
        notes.add(title = "note1", text = "text note1" )

        assertEquals(notes.delete(1), 1)
    }

    @Test
    fun add_succeeded() {
        val notes = Notes(1)
        assertEquals(notes.add(title = "note1", text = "text note1"), 1 )
    }

    @Test
    fun edit_succeeded() {
        val notes = Notes(1)
        val id = notes.add(title = "note1", text = "text note1")
        val result = notes.edit(noteId = id, title = "note2", text = "text note2" )
        val noteFromId = notes.getById(1).first()

        assertTrue(result == 1 && noteFromId.title == "note2" && noteFromId.text == "text note2")
    }

    @Test(expected = NoteNotFoundException::class)
    fun edit_failNotFound() {
        val notes = Notes(1)
        val id = notes.add(title = "note1", text = "text note1" )
        notes.edit(noteId = id + 1, title = "note2", text = "text note2" )
    }

    @Test(expected = InsufficientPermissionsForEdit::class)
    fun edit_failPrivacyView() {
        val notes = Notes(1)
        //пустые права privacyView  privacyComment
        val id = notes.add(title = "note1", text = "text note1" )
        notes.edit(noteId = id, title = "note2", text = "text note2",
            privacyView = arrayOf("nobody"), privacyComment = arrayOf("all") )
    }

    @Test(expected = InsufficientPermissionsForEdit::class)
    fun edit_failPrivacyComment() {
        val notes = Notes(1)
        //пустые права privacyView  privacyComment
        val id = notes.add(title = "note1", text = "text note1" )
        notes.edit(noteId = id, title = "note2", text = "text note2",
            privacyView = arrayOf("all"), privacyComment = arrayOf("nobody") )
    }

    @Test
    fun get_succeededDefault() {
        val notes = Notes(1)
        notes.add(title = "note1", text = "text note1" )
        val id2 = notes.add(title = "note2", text = "text note2" )
        val id3 = notes.add(title = "note3", text = "text note3" )
        notes.add(title = "note4", text = "text note4" )

        val result = notes.get(noteIds = listOf(id2, id3))
        assertTrue(result.size == 2 && result.first().id == id3 && result.last().id == id2)
    }

    @Test
    fun get_succeededAscending() {
        val notes = Notes(1)
        notes.add(title = "note1", text = "text note1" )
        val id2 = notes.add(title = "note2", text = "text note2" )
        val id3 = notes.add(title = "note3", text = "text note3" )
        notes.add(title = "note4", text = "text note4" )

        val result = notes.get(noteIds = listOf(id2, id3), count = 2, sort = true)
        assertTrue(result.size == 2 && result.first().id == id2 && result.last().id == id3)
    }

    @Test
    fun get_succeededDescending() {
        val notes = Notes(1)
        notes.add(title = "note1", text = "text note1" )
        val id2 = notes.add(title = "note2", text = "text note2" )
        val id3 = notes.add(title = "note3", text = "text note3" )
        notes.add(title = "note4", text = "text note4" )

        val result = notes.get(noteIds = listOf(id2, id3), count = 2, sort = false)
        assertTrue(result.size == 2 && result.first().id == id3 && result.last().id == id2)
    }

    @Test
    fun get_succeededQueryTooLarge() {
        val notes = Notes(1)
        notes.add(title = "note1", text = "text note1" )
        val id2 = notes.add(title = "note2", text = "text note2" )
        val id3 = notes.add(title = "note3", text = "text note3" )
        notes.add(title = "note4", text = "text note4" )

        val result = notes.get(noteIds = listOf(id2, id3), count = 10, sort = false)
        assertTrue(result.size == 2 && result.first().id == id3 && result.last().id == id2)
    }

    @Test
    fun get_succeededInvalidOffset() {
        val notes = Notes(1)
        notes.add(title = "note1", text = "text note1" )
        val id2 = notes.add(title = "note2", text = "text note2" )
        val id3 = notes.add(title = "note3", text = "text note3" )
        notes.add(title = "note4", text = "text note4" )

        val result = notes.get(noteIds = listOf(id2, id3), offset = 10, count = 10, sort = false)
        assertTrue(result.isEmpty())
    }

    @Test
    fun getById_succeeded() {
        val notes = Notes(1)
        val id1 = notes.add(title = "note1", text = "text note1" )

        val result = notes.getById(noteId = id1).first()
        assertEquals(result.id, id1)
    }

    @Test(expected = NoteNotFoundException::class)
    fun getById_idNotFound() {
        val notes = Notes(1)
        val id1 = notes.add(title = "note1", text = "text note1" )
        notes.getById(noteId = id1 + 1).first()
    }
}