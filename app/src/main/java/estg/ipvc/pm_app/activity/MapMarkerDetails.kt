package estg.ipvc.pm_app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import estg.ipvc.pm_app.API.NotesMarkerEndPoints
import estg.ipvc.pm_app.API.NotesMarkerOutputPost
import estg.ipvc.pm_app.API.ServiceBuilder
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.dataclasses.MapMarker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class MapMarkerDetails : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager

    private lateinit var tipo_problema: TextView
    private lateinit var problema: TextView
    private lateinit var coordenadas: TextView
    private lateinit var foto: ImageView
    private lateinit var lastLocation: Location

    private var editable: Boolean = false
    private var EDIT_MAP_MARKER_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        tipo_problema = findViewById<TextView>(R.id.marker_tipo_problema)
        problema = findViewById<TextView>(R.id.marker_problema)
        coordenadas = findViewById<TextView>(R.id.marker_coord)
        foto = findViewById<ImageView>(R.id.marker_foto)

        val intent = intent
        val id = intent.getIntExtra( EXTRA_ID, -1 )
        editable = intent.getBooleanExtra( EXTRA_EDITABLE, false )
        var latitude = intent.getStringExtra(EXTRA_LATITUDE).toString()
        var longitude = intent.getStringExtra(EXTRA_LONGITUDE).toString()

        coordenadas.tag = id
        lastLocation = Location("save location")
        lastLocation.latitude = latitude.toDouble()
        lastLocation.longitude = longitude.toDouble()

        val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
        val call = request.getNotesMarkerById(id)

        call.enqueue(object : Callback<List<MapMarker>> {
            override fun onResponse(call: Call<List<MapMarker>>, response: Response<List<MapMarker>>) {
                if (response.isSuccessful) {
                    val c = response.body()

                    if (c != null) {
                        for (marker in c) {
                            tipo_problema.text = marker.tipo_problema.tipo
                            problema.text = marker.problema
                            coordenadas.text = "${marker.latitude}, ${marker.longitude}"

                            Thread {
                                val url = URL("${marker.foto}")
                                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                                runOnUiThread {
                                    foto.setImageBitmap(bmp)
                                    foto.tag = marker.foto
                                }
                            }.start()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<MapMarker>>, t: Throwable) {
                Toast.makeText(this@MapMarkerDetails, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater

        if( editable ) inflater.inflate(R.menu.note_details_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_marker_btn -> {
                edit_marker()
                true
            }
            R.id.delete_marker_btn -> {
                delete_marker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( requestCode == EDIT_MAP_MARKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null ) {
            val id = data.getIntExtra(AddEditMapMarker.EXTRA_ID, -1)
            val tipoproblema = data.getStringExtra(AddEditMapMarker.EXTRA_TIPO_PROBLEMA).toString()
            val problemaa = data.getStringExtra(AddEditMapMarker.EXTRA_PROBLEMA).toString()
            val fotoo = data.getStringExtra(AddEditMapMarker.EXTRA_FOTO).toString()
            val latitude = data.getStringExtra(AddEditMapMarker.EXTRA_LATITUDE).toString()
            val longitude = data.getStringExtra(AddEditMapMarker.EXTRA_LONGITUDE).toString()

            val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
            val call = request.putNoteMarkerById(
                id,
                tipoproblema,
                problemaa,
                fotoo,
                latitude,
                longitude
            )

            call.enqueue(object : Callback<NotesMarkerOutputPost> {
                override fun onResponse(
                    call: Call<NotesMarkerOutputPost>,
                    response: Response<NotesMarkerOutputPost>
                ) {
                    if (response.isSuccessful) {
                        val c = response.body()!!

                        if (c.success) {
                            Toast.makeText(this@MapMarkerDetails, R.string.markerupdatedlabel, Toast.LENGTH_SHORT).show()

                            coordenadas.text = "$latitude, $longitude"
                            tipo_problema.text = tipoproblema
                            problema.text = problemaa

                            Thread {
                                val url = URL("${fotoo}")
                                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                                runOnUiThread {
                                    foto.setImageBitmap(bmp)
                                    foto.tag = fotoo
                                }
                            }.start()
                        }
                        else Toast.makeText(
                            this@MapMarkerDetails,
                            c.msg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                    Toast.makeText(this@MapMarkerDetails, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun edit_marker() {
        val intent = Intent(this@MapMarkerDetails, AddEditMapMarker::class.java)

        intent.putExtra(AddEditMapMarker.EXTRA_ID, coordenadas.tag.toString().toInt())
        intent.putExtra(AddEditMapMarker.EXTRA_TIPO_PROBLEMA, tipo_problema.text.toString())
        intent.putExtra(AddEditMapMarker.EXTRA_PROBLEMA, problema.text.toString())
        intent.putExtra(AddEditMapMarker.EXTRA_FOTO, foto.tag.toString())
        intent.putExtra(AddEditMapMarker.EXTRA_LATITUDE, lastLocation.latitude.toString())
        intent.putExtra(AddEditMapMarker.EXTRA_LONGITUDE, lastLocation.longitude.toString())
        startActivityForResult(intent, EDIT_MAP_MARKER_REQUEST_CODE)
    }

    fun delete_marker() {
        val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
        val call = request.deleteNoteMarker(
            coordenadas.tag.toString().toInt()
        )

        call.enqueue(object : Callback<NotesMarkerOutputPost> {
            override fun onResponse(
                call: Call<NotesMarkerOutputPost>,
                response: Response<NotesMarkerOutputPost>
            ) {
                if (response.isSuccessful) {
                    val c = response.body()!!

                    if (c.success) {
                        Toast.makeText(this@MapMarkerDetails, R.string.markerdeletedlabel, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@MapMarkerDetails, MapActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else Toast.makeText(this@MapMarkerDetails, R.string.markerdeletedfailedlabel, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                Toast.makeText(this@MapMarkerDetails, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun map( view: View) {
        val intent = Intent( this@MapMarkerDetails, MapActivity::class.java )
        startActivity( intent )
        finish()
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
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                    this,
                    accelerometer,
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
            val layout = findViewById<ConstraintLayout>(R.id.note_details_layout)
            val marker_coord = findViewById<TextView>(R.id.marker_coord)
            val marker_tipo_problema = findViewById<TextView>(R.id.marker_tipo_problema)
            val marker_problema = findViewById<TextView>(R.id.marker_problema)

            Log.d("SENSORS", "onSensorChanged: TYPE_LIGHT: ${event.values[0]}")
            if (event.values[0] > 5000) {
                layout.setBackgroundColor(Color.parseColor("#000000"))
                marker_coord.setTextColor(Color.parseColor("#FFFFFF"))
                marker_tipo_problema.setTextColor(Color.parseColor("#FFFFFF"))
                marker_problema.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else {
                layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                marker_coord.setTextColor(Color.parseColor("#000000"))
                marker_tipo_problema.setTextColor(Color.parseColor("#000000"))
                marker_problema.setTextColor(Color.parseColor("#000000"))
            }
        }
        /*else if( event.sensor.type == Sensor.TYPE_ACCELEROMETER ) {
            val xChange: Float = history.get(0) - event.values[0]
            val yChange: Float = history.get(1) - event.values[1]

            history[0] = event.values[0]
            history[1] = event.values[1]

            if (xChange > 0) {
                direction[0] = "LEFT"
            } else if (xChange < 0) {
                direction[0] = "RIGHT"
            }

            if (yChange > 1) {
                direction[1] = "DOWN"
            } else if (yChange < -1) {
                direction[1] = "UP"
            }

            builder.setLength(0)
            builder.append("x: ")
            builder.append(direction.get(0))
            builder.append(" y: ")
            builder.append(direction.get(1))

            findViewById<TextView>(R.id.text).setText(builder.toString())
            Log.d("SENSORS", "onSensorChanged: TYPE_ACCELEROMETER: ${event.values}")
        }*/
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    companion object {
        const val EXTRA_ID = "estg.ipvc.pm_app.activity.notedetails.EXTRA_ID"
        const val EXTRA_EDITABLE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_EDITABLE"

        const val EXTRA_LATITUDE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_LONGITUDE"
    }
}