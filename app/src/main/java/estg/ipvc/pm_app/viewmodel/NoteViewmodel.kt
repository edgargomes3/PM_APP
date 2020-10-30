package estg.ipvc.pm_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import estg.ipvc.pm_app.database.*
import estg.ipvc.pm_app.entity.*
import estg.ipvc.pm_app.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    init {
        val notesDao = NoteDB.getDatabase(application, viewModelScope).NoteDao()
        repository = NoteRepository(notesDao)
        allNotes = repository.allNotes
    }
    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNote(note)
    }
    fun updateNote(numero: String, name: String) = viewModelScope.launch{
        repository.updateNote(numero, name)
    }
    fun deleteNote(note: Note) = viewModelScope.launch{
        repository.deleteNote(note)
    }
    fun deleteAllNotes()= viewModelScope.launch(Dispatchers.IO){
        repository.deleteAllNotes()
    }
}


