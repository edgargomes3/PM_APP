package estg.ipvc.pm_app.repository

import androidx.lifecycle.LiveData
import estg.ipvc.pm_app.dao.*
import estg.ipvc.pm_app.entity.*

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getNotes()

    suspend fun insertNote( note: Note ) {
        noteDao.insert( note )
    }

    suspend fun updateNote( note: Note ) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote( note: Note ) {
        noteDao.deleteNote( note )
    }

    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }
}
