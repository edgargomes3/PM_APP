package estg.ipvc.pm_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import estg.ipvc.pm_app.adapters.*
import estg.ipvc.pm_app.dataclasses.*
import kotlinx.android.synthetic.main.activity_notes.*

class NotesActivity : AppCompatActivity() {
    private lateinit var myList: ArrayList<note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        myList = ArrayList<note>()

        for ( i in 0 until 5 ) {
            myList.add( note("texto1 $i", "texto2 $i") )
        }
        notes_recycler_view.adapter = LineAdapter(myList)
        notes_recycler_view.layoutManager = LinearLayoutManager( this )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.notes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when ( item.itemId ) {
            R.id.create_new_note -> {
                Toast.makeText(this, "create_new_note", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.edit_note -> {
                Toast.makeText(this, "edit_note", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.delete_note -> {
                Toast.makeText(this, "delete_note", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun create_note( view: View) {
        myList.add(0, note("teste1", "teste2"))
        notes_recycler_view.adapter?.notifyDataSetChanged()
    }
}