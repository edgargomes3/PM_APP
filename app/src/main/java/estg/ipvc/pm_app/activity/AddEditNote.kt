package estg.ipvc.pm_app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import estg.ipvc.pm_app.R

class AddEditNote : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager

    private lateinit var editNoteTitleView: EditText
    private lateinit var editNoteTextView: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

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
            if( id != -1 ) {
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
            R.id.save_note_btn -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()

        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)?.also { light ->
            sensorManager.registerListener(
                    this,
                    light,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val layout = findViewById<RelativeLayout>(R.id.addeditnote_layout)
            val new_note_title = findViewById<EditText>(R.id.new_note_title)
            val new_note_text = findViewById<EditText>(R.id.new_note_text)

            Log.d("SENSORS", "onSensorChanged: TYPE_LIGHT: ${event.values[0]}")
            if (event.values[0] > 5000) {
                layout.setBackgroundColor(Color.parseColor("#000000"))
                new_note_title.setTextColor(Color.parseColor("#FFFFFF"))
                new_note_title.setHintTextColor(Color.parseColor("#FFFFFF"))
                new_note_text.setTextColor(Color.parseColor("#FFFFFF"))
                new_note_text.setHintTextColor(Color.parseColor("#FFFFFF"))
            }
            else {
                layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                new_note_title.setTextColor(Color.parseColor("#000000"))
                new_note_title.setHintTextColor(Color.parseColor("#000000"))
                new_note_text.setTextColor(Color.parseColor("#000000"))
                new_note_text.setHintTextColor(Color.parseColor("#000000"))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    companion object {
        const val EXTRA_ID = "estg.ipvc.pm_app.activity.addeditnote.EXTRA_ID"
        const val EXTRA_TITLE = "estg.ipvc.pm_app.activity.addeditnote.EXTRA_TITLE"
        const val EXTRA_TEXT = "estg.ipvc.pm_app.activity.addeditnote.EXTRA_TEXT"
    }
}