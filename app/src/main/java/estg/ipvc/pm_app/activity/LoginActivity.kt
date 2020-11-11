package estg.ipvc.pm_app.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import estg.ipvc.pm_app.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun login( view: View ) {
        val intent = Intent( this, MapActivity::class.java )
        startActivity(intent)
    }

    fun show_notes( view: View ) {
        val intent = Intent( this, NoteActivity::class.java )
        startActivity(intent)
    }
}