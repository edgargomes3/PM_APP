package estg.ipvc.pm_app.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        /*locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                Log.d("****LOCATION****", "new location received - " + loc.latitude + " -" + loc.longitude)
            }
        }

       createLocationRequest()*/
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
                val intent = Intent(this@MapActivity, AddMapMarker::class.java)
                startActivity(intent)
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

    /*private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates( locationRequest, locationCallback, null )
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest()
        // interval specifies the rate at which your app will like to receive updates.
        locationRequest.interval = 10000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("****LOCATION****", "onPause - removeLocationUpdates")
    }

    public override fun onResume() {
        super.onResume()
        startLocationUpdates()
        Log.d("****LOCATION****", "onResume - startLocationUpdates")
    }*/
}