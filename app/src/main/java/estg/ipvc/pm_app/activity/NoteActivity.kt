package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.adapters.*
import estg.ipvc.pm_app.entity.*
import estg.ipvc.pm_app.viewmodel.*
import kotlinx.android.synthetic.main.activity_notes.*

private lateinit var noteViewModel: NoteViewModel

class NoteActivity : AppCompatActivity(), NoteAdapter.OnItemClickListener {
    private val AddNoteRequestCode = 1
    private val EditNoteRequestCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view)
        val adapter = NoteAdapter(this, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.notes_new).setOnClickListener {
            val intent = Intent(this, AddEditNote::class.java)
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
                Toast.makeText(this@NoteActivity, R.string.notedeletelabel, Toast.LENGTH_SHORT).show()
            }

        }

        val itemTouchHelper = ItemTouchHelper( itemTouchHelperCallback )
        itemTouchHelper.attachToRecyclerView( notes_recycler_view )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddNoteRequestCode && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra(AddEditNote.EXTRA_TITLE).toString()
            val text = data?.getStringExtra(AddEditNote.EXTRA_TEXT).toString()
            val note = Note(title = (title), text = (text))

            noteViewModel.insertNote(note)
            Toast.makeText(this@NoteActivity, R.string.notecreatedlabel, Toast.LENGTH_SHORT).show()
        }
        else if (requestCode == EditNoteRequestCode && resultCode == Activity.RESULT_OK) {
            val id = data?.getIntExtra( AddEditNote.EXTRA_ID, -1 )

            if( id == -1 ) {
                Toast.makeText(this@NoteActivity, R.string.noupdatelabel, Toast.LENGTH_SHORT).show()
                return
            }

            val title = data?.getStringExtra( AddEditNote.EXTRA_TITLE ).toString()
            val text = data?.getStringExtra( AddEditNote.EXTRA_TEXT ).toString()
            val note = Note(id, title, text)

            noteViewModel.updateNote(note)
            Toast.makeText(this@NoteActivity, R.string.noteupdatedlabel, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
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

    override fun onItemClicked(note: Note ) {
        val intent = Intent( this, AddEditNote::class.java)
        intent.putExtra(AddEditNote.EXTRA_ID, note.id)
        intent.putExtra(AddEditNote.EXTRA_TITLE, note.title)
        intent.putExtra(AddEditNote.EXTRA_TEXT, note.text)
        startActivityForResult(intent, EditNoteRequestCode)
    }

}