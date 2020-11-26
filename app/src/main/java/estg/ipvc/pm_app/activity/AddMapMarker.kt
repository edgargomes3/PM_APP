package estg.ipvc.pm_app.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import estg.ipvc.pm_app.API.NotesMarkerEndPoints
import estg.ipvc.pm_app.API.NotesMarkerOutputPost
import estg.ipvc.pm_app.API.ServiceBuilder
import estg.ipvc.pm_app.R
import kotlinx.android.synthetic.main.activity_add_map_marker.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddMapMarker : AppCompatActivity() {
    private lateinit var coordenadas: TextView
    private lateinit var problema: EditText
    private lateinit var tipoproblema: EditText
    private lateinit var lastLocation: Location
    private lateinit var foto: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val REQUEST_IMAGE_GALLERY = 2
    private val REQUEST_IMAGE_CAMERA = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_map_marker)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        coordenadas = findViewById<TextView>(R.id.new_marker_coord)
        tipoproblema = findViewById<EditText>(R.id.new_marker_tipo_problema)
        problema = findViewById<EditText>(R.id.new_marker_problema)
        foto = findViewById<ImageView>(R.id.new_marker_foto)

        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        else {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location->
                if ( location != null ) {
                    lastLocation = location
                    coordenadas.text = "${lastLocation.latitude}, ${lastLocation.longitude}"
                }
                else Toast.makeText(this, R.string.nolastlocationlabel, Toast.LENGTH_LONG).show()
            }
        }

        new_marker_foto.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    if( permission != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
                    }
                    else {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( requestCode == REQUEST_IMAGE_CAMERA && resultCode == Activity.RESULT_OK && data != null ) {
            val bitmap = data.extras?.get("data") as Bitmap
            foto.setImageBitmap(bitmap)

            val storage = Environment.getExternalStorageDirectory()
            val dir = File(storage.absolutePath + "/fileToUpload")
            dir.mkdirs()

            val fileName = String.format("%d.png", System.currentTimeMillis())
            val outFile = File(dir, fileName)

            val outStream = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()

            foto.tag = outFile.path
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_note_btn -> {
                saveNota()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveNota(){
        if ( TextUtils.isEmpty(tipoproblema.text) ) {
            Toast.makeText(this, R.string.fieldproblemtypeemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( TextUtils.isEmpty(problema.text) ) {
            Toast.makeText(this, R.string.fieldproblememptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( TextUtils.isEmpty(foto.tag.toString()) ) {
            Toast.makeText(this, R.string.fieldfotoemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else {
            val sharedPref: SharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val username = sharedPref.getString(getString(R.string.automatic_login_username), null)

            // CREATE FILE FROM PATH
            val file = File("${foto.tag}")
            val fileName = String.format("%d.png", System.currentTimeMillis())

            // CREATE MULTIPART BODY TO UPLOAD
            val requestFile: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val fileToUpload: MultipartBody.Part = MultipartBody.Part.createFormData("image", fileName, requestFile)

            val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
            val call = request.postNotesMarkerImage(
                    fileToUpload
            )

            call.enqueue(object : Callback<NotesMarkerOutputPost> {
                override fun onResponse(call: Call<NotesMarkerOutputPost>, response: Response<NotesMarkerOutputPost>) {
                    if (response.isSuccessful) {
                        val c = response.body()!!

                        if ( c.success ) {
                            val call = request.postNotesMarker(
                                    tipoproblema.text.toString(),
                                    problema.text.toString(),
                                    c.msg,
                                    lastLocation.latitude.toString(),
                                    lastLocation.longitude.toString(),
                                    username.toString()
                            )

                            call.enqueue(object : Callback<NotesMarkerOutputPost> {
                                override fun onResponse(call: Call<NotesMarkerOutputPost>, response: Response<NotesMarkerOutputPost>) {
                                    if (response.isSuccessful) {
                                        val c = response.body()!!

                                        if ( c.success ) {
                                            Toast.makeText(this@AddMapMarker, R.string.noteinsertlabel, Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@AddMapMarker, MapActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        else Toast.makeText(this@AddMapMarker, R.string.noteinsertfaillabel, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                                    Toast.makeText(this@AddMapMarker, "${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        else Toast.makeText(this@AddMapMarker, "${R.string.imguplabel}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                    Toast.makeText(this@AddMapMarker, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}