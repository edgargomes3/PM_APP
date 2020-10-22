package estg.ipvc.pm_app.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import estg.ipvc.pm_app.entity.*

@Dao
interface NoteDao {
    @Query("SELECT * from note_table ORDER BY name ASC")
    fun getAlphabetizedNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Query("UPDATE note_table SET numero = :numero WHERE name ='Ana'")
    suspend fun updatenumber(numero: String)

    @Query("UPDATE note_table SET numero = :numero, name =:name")
    suspend fun updateNote(numero: String, name: String)
}
