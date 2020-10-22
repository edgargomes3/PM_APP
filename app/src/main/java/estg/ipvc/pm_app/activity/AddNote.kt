package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import estg.ipvc.pm_app.R

class AddNote : AppCompatActivity() {

    private lateinit var editWordView: EditText
    private lateinit var editnumberView: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        editWordView = findViewById(R.id.new_note_title)
        editnumberView = findViewById(R.id.new_note_text)

        val button = findViewById<Button>(R.id.new_note_btn)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editWordView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val word = editWordView.text.toString()
                val numero = editnumberView.text.toString()
                replyIntent.putExtra(EXTRA_REPLY, word)
                replyIntent.putExtra(EXTRA1_REPLY, numero)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
        const val EXTRA1_REPLY = "com.example.android.wordlistsql.REPLY1"
    }
}