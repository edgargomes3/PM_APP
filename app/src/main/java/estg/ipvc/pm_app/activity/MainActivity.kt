package estg.ipvc.pm_app.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import estg.ipvc.pm_app.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)
    }

    fun show_notes( view: View ) {
        val intent = Intent( this, NoteActivity::class.java )
        startActivity(intent)
    }
}