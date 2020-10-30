package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.adapters.*
import estg.ipvc.pm_app.entity.*
import estg.ipvc.pm_app.viewmodel.*
import kotlinx.android.synthetic.main.activity_notes.*
import kotlinx.android.synthetic.main.notes_helper.*

private lateinit var noteViewModel: NoteViewModel

class NoteActivity : AppCompatActivity() {
    private val AddNoteRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view)
        val adapter = NoteAdapter(this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.notes_new).setOnClickListener {
            val intent = Intent(this, AddNote::class.java)
            startActivityForResult(intent, AddNoteRequestCode)
        }

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteViewModel.allNotes.observe(this, { notes ->
            notes?.let { adapter.setNote(it) }
        })

        // SWIPE TO DELETE
        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                noteViewModel.deleteNote( adapter.getNoteAt(viewHolder.adapterPosition) )
            }

        }

        val itemTouchHelper = ItemTouchHelper( itemTouchHelperCallback )
        itemTouchHelper.attachToRecyclerView( notes_recycler_view )
    }

    fun checkboxClicked(view: View) {
        if ( view is CheckBox ) {
            val sharedPref : SharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            with(sharedPref.edit()) {
                putBoolean(getString(R.string.sound), view.isChecked)
                commit()
            }
            Log.d("SHAREDPREF", "Changed to ${view.isChecked}")

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddNoteRequestCode && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra(AddNote.EXTRA_TITLE).toString()
            val text = data?.getStringExtra(AddNote.EXTRA_TEXT).toString()
            val note = Note(title = (title), text = (text))

            noteViewModel.insertNote(note)
            Toast.makeText(this, R.string.notecreatedlabel, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater=menuInflater
        inflater.inflate(R.menu.notes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_all_notes -> {
                noteViewModel.deleteAllNotes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}