package estg.ipvc.pm_app.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import estg.ipvc.pm_app.entity.*

@Dao
interface NoteDao {
    @Query("SELECT * from note_table ORDER BY id ASC")
    fun getNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Delete
    suspend fun deleteNote( note: Note )

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Query("UPDATE note_table SET text = :text, title = :title")
    suspend fun updateNote( text: String, title: String )
}
