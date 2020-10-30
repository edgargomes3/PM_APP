package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import estg.ipvc.pm_app.R

class AddEditNote : AppCompatActivity() {

    private lateinit var editNoteTitleView: EditText
    private lateinit var editNoteTextView: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        editNoteTitleView = findViewById(R.id.new_note_title)
        editNoteTextView = findViewById(R.id.new_note_text)

        val intent = intent
        editNoteTitleView.setText( intent.getStringExtra(EXTRA_TITLE) )
        editNoteTextView.setText( intent.getStringExtra(EXTRA_TEXT) )
    }

    fun saveNote() {
        val replyIntent = Intent()
        if ( TextUtils.isEmpty(editNoteTitleView.text) ) {
            Toast.makeText(this, R.string.fieldtitleemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( TextUtils.isEmpty(editNoteTextView.text) ) {
            Toast.makeText(this, R.string.fieldtextemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else {
            val title = editNoteTitleView.text.toString()
            val text = editNoteTextView.text.toString()

            val id = intent.getIntExtra( EXTRA_ID, -1 )
            if( id != 1 ) {
                replyIntent.putExtra(EXTRA_ID, id)
            }
            
            replyIntent.putExtra(EXTRA_TITLE, title)
            replyIntent.putExtra(EXTRA_TEXT, text)
            setResult(Activity.RESULT_OK, replyIntent)
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_note_btn -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_ID = "estg.ipvc.pm_app.activity.EXTRA_ID"
        const val EXTRA_TITLE = "estg.ipvc.pm_app.activity.EXTRA_TITLE"
        const val EXTRA_TEXT = "estg.ipvc.pm_app.activity.EXTRA_TEXT"
    }
}