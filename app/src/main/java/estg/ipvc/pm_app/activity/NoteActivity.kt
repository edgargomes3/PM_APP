package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import estg.ipvc.pm_app.adapters.*
import estg.ipvc.pm_app.entity.*
import estg.ipvc.pm_app.viewmodel.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import estg.ipvc.pm_app.R

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

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteViewModel.allNotes.observe(this, { notes ->
            notes?.let{adapter.setNote(it)}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddNoteRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(AddNote.EXTRA_REPLY)?.let {
                val name = it
                data.getStringExtra(AddNote.EXTRA1_REPLY)?.let {
                    val note = Note(name = (name), numero = (it))

                    noteViewModel.insertNote(note)
                }
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Campo nao inserido",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater=menuInflater
        inflater.inflate(R.menu.notes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.create_new_note -> {
                val intent = Intent(this, AddNote::class.java)
                startActivityForResult(intent, AddNoteRequestCode)
                true
            }

            /*R.id.updateana -> {
                noteViewModel.updatenumber("967")
                true
            }
            R.id.updatecon ->
            {
                val intent = Intent(this@NoteActivity, UpdateActivity::class.java)
                this.startActivity(Intent(this,UpdateActivity::class.java))
                return true
            }*/
            R.id.delete_all_notes -> {
                noteViewModel.deleteAllNotes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}