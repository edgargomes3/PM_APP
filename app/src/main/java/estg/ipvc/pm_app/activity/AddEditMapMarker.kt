package estg.ipvc.pm_app.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.net.URL

class AddEditMapMarker : AppCompatActivity() {
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
        val id = intent.getIntExtra(EXTRA_ID, -1)
        val latitude = intent.getStringExtra(EXTRA_LATITUDE).toString()
        val longitude = intent.getStringExtra(EXTRA_LONGITUDE).toString()
        var username = intent.getStringExtra(EXTRA_USERNAME).toString()

        if( id != -1 ) {
            coordenadas.tag = id
            tipoproblema.tag = intent.getStringExtra(EXTRA_TIPO_PROBLEMA).toString()
            problema.setText(intent.getStringExtra(EXTRA_PROBLEMA).toString())
            foto.tag = intent.getStringExtra(EXTRA_FOTO).toString()

            Thread {
                val url = URL("${foto.tag}")
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                runOnUiThread {
                    foto.setImageBitmap(bmp)
                }
            }.start()
        }
        else coordenadas.tag = username

        coordenadas.text = "${latitude}, ${longitude}"
        lastLocation = Location("save location")
        lastLocation.latitude = latitude.toDouble()
        lastLocation.longitude = longitude.toDouble()

        var tiposProblema = ArrayList<String>()
        tiposProblema.add( "" )

        val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
        val call = request.getTiposProblema()

        call.enqueue(object : Callback<List<TipoProblema>> {
            override fun onResponse(call: Call<List<TipoProblema>>, response: Response<List<TipoProblema>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    var tipoProblemaIndex = -1

                    for( tipo in c ) {
                        tiposProblema.add("${tipo.tipo}")
                        if( tipoproblema.tag != null ) {
                            if (TextUtils.equals(tipo.tipo.toString(), tipoproblema.tag.toString())) {
                                tipoProblemaIndex = tiposProblema.lastIndex
                            }
                        }
                    }

                    adapter = ArrayAdapter(this@AddEditMapMarker, android.R.layout.simple_spinner_item, tiposProblema )
                    tipoproblema.adapter = adapter

                    if( tipoProblemaIndex > -1 ) tipoproblema.setSelection(tipoProblemaIndex)
                }
            }

            override fun onFailure(call: Call<List<TipoProblema>>, t: Throwable) {
                Toast.makeText(this@AddEditMapMarker, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        tipoproblema.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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
                saveMarker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveMarker() {
        if ( tipoproblema.selectedItemId <= 0 ) {
            Toast.makeText(this@AddEditMapMarker, R.string.fieldproblemtypeemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( TextUtils.isEmpty(problema.text) ) {
            Toast.makeText(this@AddEditMapMarker, R.string.fieldproblememptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( foto.tag == null ) {
            Toast.makeText(this@AddEditMapMarker, R.string.fieldfotoemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else {
            val regex = "(https://dolabriform-reactio.000webhostapp.com/).+".toRegex()
            val result = regex.matchEntire(foto.tag.toString())?.value

            if( result == null ) {
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

                            if (c.success) {
                                Toast.makeText(this@AddEditMapMarker, R.string.imageuploadlabel, Toast.LENGTH_SHORT).show()
                                foto.tag = c.msg
                                if( coordenadas.tag != null) {
                                    val replyIntent = Intent()

                                    replyIntent.putExtra(EXTRA_TIPO_PROBLEMA, tipoproblema.selectedItem.toString())
                                    replyIntent.putExtra(EXTRA_PROBLEMA, problema.text.toString())
                                    replyIntent.putExtra(EXTRA_FOTO, foto.tag.toString())
                                    replyIntent.putExtra(EXTRA_LATITUDE, lastLocation.latitude.toString())
                                    replyIntent.putExtra(EXTRA_LONGITUDE, lastLocation.longitude.toString())

                                    if( coordenadas.tag.toString().toIntOrNull() != null ) replyIntent.putExtra(EXTRA_ID, coordenadas.tag.toString().toInt())
                                    else replyIntent.putExtra(EXTRA_USERNAME, coordenadas.tag.toString())
                                    setResult(Activity.RESULT_OK, replyIntent)
                                    finish()
                                }
                            }
                            else Toast.makeText(this@AddEditMapMarker, R.string.imageuploadfailedlabel, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                        Toast.makeText(this@AddEditMapMarker, "${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else {
                if( coordenadas.tag != null) {
                    val replyIntent = Intent()

                    replyIntent.putExtra(EXTRA_TIPO_PROBLEMA, tipoproblema.selectedItem.toString())
                    replyIntent.putExtra(EXTRA_PROBLEMA, problema.text.toString())
                    replyIntent.putExtra(EXTRA_FOTO, foto.tag.toString())
                    replyIntent.putExtra(EXTRA_LATITUDE, lastLocation.latitude.toString())
                    replyIntent.putExtra(EXTRA_LONGITUDE, lastLocation.longitude.toString())

                    if( coordenadas.tag.toString().toIntOrNull() != null ) replyIntent.putExtra(EXTRA_ID, coordenadas.tag.toString().toInt())
                    else replyIntent.putExtra(EXTRA_USERNAME, coordenadas.tag.toString())
                    setResult(Activity.RESULT_OK, replyIntent)
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_ID = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_ID"
        const val EXTRA_TIPO_PROBLEMA = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_TIPO_PROBLEMA"
        const val EXTRA_PROBLEMA = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_PROBLEMA"
        const val EXTRA_FOTO = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_FOTO"
        const val EXTRA_LATITUDE = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_LONGITUDE"
        const val EXTRA_USERNAME = "estg.ipvc.pm_app.activity.addmapmarker.EXTRA_USERNAME"
    }
}