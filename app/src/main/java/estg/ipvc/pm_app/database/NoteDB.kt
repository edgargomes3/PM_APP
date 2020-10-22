package estg.ipvc.pm_app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import estg.ipvc.pm_app.dao.*
import estg.ipvc.pm_app.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDB : RoomDatabase(){
    abstract fun NoteDao(): NoteDao
    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback(){

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var noteDao = database.NoteDao()

                    /* noteDao.deleteAll()
                    var name = Note(1,"Gabriel", "963852741")
                    noteDao.insert(name)
                    name= Note(2, "Edgar", "147258369")
                    noteDao.insert(name) */
                }
            }
        }
    }
    companion object{
        @Volatile
        private var INSTANCE: NoteDB? = null
        fun getDatabase(context: Context, scope: CoroutineScope): NoteDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDB::class.java,
                    "notes_database"
                )
                    //.fallbackToDestructiveMigration()
                    .addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
















