package estg.ipvc.pm_app.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import estg.ipvc.pm_app.API.*
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.dataclasses.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var user: String

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val ADD_MAP_MARKER_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val sharedPref: SharedPreferences = getSharedPreferences( getString(R.string.preference_file_key), Context.MODE_PRIVATE )
        user = sharedPref.getString(getString(R.string.automatic_login_username), null)!!

        val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
        val call = request.getNotesMarker()
        var position: LatLng

        call.enqueue(object : Callback<List<Nota>> {
            override fun onResponse(call: Call<List<Nota>>, response: Response<List<Nota>>) {
                if (response.isSuccessful) {
                    val c = response.body()

                    if( c != null ){
                        for( note in c ){
                            position = LatLng( note.latitude.toDouble(), note.longitude.toDouble() )
                            mMap.addMarker(MarkerOptions().position(position).title(note.problema))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Nota>>, t: Throwable) {
                Toast.makeText(this@MapActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    lastLocation = location
                    var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                    Log.d("****LOCATION****", "new location received - " + loc.latitude + " -" + loc.longitude)
                }
            }
        }

        getLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( requestCode == ADD_MAP_MARKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null ) {

            val tipoproblema = data.getStringExtra(AddMapMarker.EXTRA_TIPO_PROBLEMA).toString()
            val problema = data.getStringExtra(AddMapMarker.EXTRA_PROBLEMA).toString()
            val foto = data.getStringExtra(AddMapMarker.EXTRA_FOTO).toString()
            val latitude = data.getStringExtra(AddMapMarker.EXTRA_LATITUDE).toString()
            val longitude = data.getStringExtra(AddMapMarker.EXTRA_LONGITUDE).toString()
            val username = data.getStringExtra(AddMapMarker.EXTRA_USERNAME).toString()

            val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
            val call = request.postNotesMarker(
                    tipoproblema,
                    problema,
                    foto,
                    latitude,
                    longitude,
                    username
            )

            call.enqueue(object : Callback<NotesMarkerOutputPost> {
                override fun onResponse(call: Call<NotesMarkerOutputPost>, response: Response<NotesMarkerOutputPost>) {
                    if (response.isSuccessful) {
                        val c = response.body()!!

                        if ( c.success ) Toast.makeText(this@MapActivity, R.string.noteinsertlabel, Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this@MapActivity, R.string.noteinsertfaillabel, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NotesMarkerOutputPost>, t: Throwable) {
                    Toast.makeText(this@MapActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.map_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_marker_btn -> {
                new_marker()
                true
            }
            R.id.logout_btn -> {
                val sharedPref: SharedPreferences = getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE )
                with ( sharedPref.edit() ) {
                    putBoolean(getString(R.string.automatic_login_check), false )
                    putString(getString(R.string.automatic_login_username), null )
                    commit()
                }

                val intent = Intent(this@MapActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun new_marker() {
        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        else {
            val intent = Intent( this@MapActivity, AddMapMarker::class.java)


            intent.putExtra(AddMapMarker.EXTRA_LATITUDE, lastLocation.latitude.toString())
            intent.putExtra(AddMapMarker.EXTRA_LONGITUDE, lastLocation.longitude.toString())
            intent.putExtra(AddMapMarker.EXTRA_USERNAME, user)
            startActivityForResult(intent, ADD_MAP_MARKER_REQUEST_CODE)
        }
    }

    private fun getLocationUpdates()
    {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    lastLocation = locationResult.lastLocation
                    var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                    Log.d("****LOCATION****", "New Location Received - " + loc.latitude + " -" + loc.longitude)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        Log.d("****LOCATION****", "onPause - removeLocationUpdates")
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        Log.d("****LOCATION****", "onResume - startLocationUpdates")
    }
}