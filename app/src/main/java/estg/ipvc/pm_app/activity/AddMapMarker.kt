package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import estg.ipvc.pm_app.API.NotesMarkerEndPoints
import estg.ipvc.pm_app.API.NotesMarkerOutputPost
import estg.ipvc.pm_app.API.ServiceBuilder
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.dataclasses.TipoProblema
import kotlinx.android.synthetic.main.activity_add_map_marker.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Array as Array1
import kotlin.collections.arrayListOf as arrayListOf1

class AddMapMarker : AppCompatActivity() {
    private lateinit var coordenadas: TextView
    private lateinit var problema: EditText
    private lateinit var tipoproblema: Spinner
    private lateinit var lastLocation: Location
    private lateinit var foto: ImageView
    private lateinit var adapter: ArrayAdapter<String>
    private val REQUEST_IMAGE_CAMERA = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_map_marker)

        coordenadas = findViewById<TextView>(R.id.new_marker_coord)
        tipoproblema = findViewById<Spinner>(R.id.new_marker_tipo_problema)
        problema = findViewById<EditText>(R.id.new_marker_problema)
        foto = findViewById<ImageView>(R.id.new_marker_foto)

        val intent = intent
        var latitude = intent.getStringExtra(EXTRA_LATITUDE).toString()
        var longitude = intent.getStringExtra(EXTRA_LONGITUDE).toString()
        var username = intent.getStringExtra(EXTRA_USERNAME).toString()
        coordenadas.text = "${latitude}, ${longitude}"
        coordenadas.tag = username
        lastLocation = Location("save location")
        lastLocation.latitude = latitude.toDouble()
        lastLocation.longitude = longitude.toDouble()


        var tiposProblema = ArrayList<String>()
        tiposProblema.add("Selecione")

        val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
        val call = request.getTiposProblema()

        call.enqueue(object : Callback<List<TipoProblema>> {
            override fun onResponse(call: Call<List<TipoProblema>>, response: Response<List<TipoProblema>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!

                    for( tipo in c ) {
                        tiposProblema.add("${tipo.tipo.toString()}")
                    }
                }
            }

            override fun onFailure(call: Call<List<TipoProblema>>, t: Throwable) {
                Toast.makeText(this@AddMapMarker, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposProblema )
        tipoproblema.adapter = adapter

        tipoproblema.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
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

    fun saveNota() {
        if ( tipoproblema.selectedItemId <= 0 ) {
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
            val file = File("${foto.tag}")
            val fileName = String.format("%d.png", System.currentTimeMillis())

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
                            Toast.makeText(this@AddMapMarker, R.string.imageuploadlabel, Toast.LENGTH_SHORT).show()
                            val replyIntent = Intent()

                            replyIntent.putExtra(EXTRA_TIPO_PROBLEMA, tipoproblema.selectedItem.toString())
                            replyIntent.putExtra(EXTRA_PROBLEMA, problema.text.toString())
                            replyIntent.putExtra(EXTRA_FOTO, c.msg)
                            replyIntent.putExtra(EXTRA_LATITUDE, lastLocation.latitude.toString())
                            replyIntent.putExtra(EXTRA_LONGITUDE, lastLocation.longitude.toString())
                            replyIntent.putExtra(EXTRA_USERNAME, coordenadas.tag.toString())
                            setResult(Activity.RESULT_OK, replyIntent)
                            finish()
                        }
                        else Toast.makeText(this@AddMapMarker, R.string.imageuploadfailedlabel, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                    Toast.makeText(this@AddMapMarker, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    companion object {
        const val EXTRA_TIPO_PROBLEMA = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_TIPO_PROBLEMA"
        const val EXTRA_PROBLEMA = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_PROBLEMA"
        const val EXTRA_FOTO = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_FOTO"
        const val EXTRA_LATITUDE = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_LONGITUDE"
        const val EXTRA_USERNAME = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_USERNAME"
    }
}