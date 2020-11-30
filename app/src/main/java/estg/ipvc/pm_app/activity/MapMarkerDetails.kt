package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import estg.ipvc.pm_app.API.NotesMarkerEndPoints
import estg.ipvc.pm_app.API.NotesMarkerOutputPost
import estg.ipvc.pm_app.API.ServiceBuilder
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.dataclasses.MapMarker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class MapMarkerDetails : AppCompatActivity() {

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

    companion object {
        const val EXTRA_ID = "estg.ipvc.pm_app.activity.notedetails.EXTRA_ID"
        const val EXTRA_EDITABLE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_EDITABLE"

        const val EXTRA_LATITUDE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_LONGITUDE"
    }
}