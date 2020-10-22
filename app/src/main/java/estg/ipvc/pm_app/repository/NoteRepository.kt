package estg.ipvc.pm_app.repository

import androidx.lifecycle.LiveData
import estg.ipvc.pm_app.dao.*
import estg.ipvc.pm_app.entity.*

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAlphabetizedNotes()

    suspend fun deleteAllNotes(){
        noteDao.deleteAllNotes()
    }

    suspend fun insertNote(note: Note) {
        noteDao.insert(note)
    }

    suspend fun updatenumber(numero: String){
        noteDao.updatenumber(numero)
    }

    suspend fun updateNote(numero: String, name: String){
        noteDao.updateNote(numero, name)
    }
}
