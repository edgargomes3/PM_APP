package estg.ipvc.pm_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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

    fun insert( view: View) {
        myList.add(0, note("teste1", "teste2"))
        notes_recycler_view.adapter?.notifyDataSetChanged()
    }
}