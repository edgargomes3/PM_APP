package estg.ipvc.pm_app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import estg.ipvc.pm_app.API.NotesMarkerEndPoints
import estg.ipvc.pm_app.API.NotesMarkerOutputPost
import estg.ipvc.pm_app.API.ServiceBuilder
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.dataclasses.MapMarker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout


class MapActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var history: FloatArray
    private lateinit var direction: String

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

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        history = FloatArray(2)
        direction = ""

        val sharedPref: SharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        )
        user = sharedPref.getString(getString(R.string.automatic_login_username), null)!!

        load_marker(null, null)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    lastLocation = location
                    var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                    Log.d(
                            "****LOCATION****",
                            "new location received - " + loc.latitude + " -" + loc.longitude
                    )
                }
            }
        }

        getLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( requestCode == ADD_MAP_MARKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null ) {

            val tipoproblema = data.getStringExtra(AddEditMapMarker.EXTRA_TIPO_PROBLEMA).toString()
            val problema = data.getStringExtra(AddEditMapMarker.EXTRA_PROBLEMA).toString()
            val foto = data.getStringExtra(AddEditMapMarker.EXTRA_FOTO).toString()
            val latitude = data.getStringExtra(AddEditMapMarker.EXTRA_LATITUDE).toString()
            val longitude = data.getStringExtra(AddEditMapMarker.EXTRA_LONGITUDE).toString()
            val username = data.getStringExtra(AddEditMapMarker.EXTRA_USERNAME).toString()
            val position = LatLng( latitude.toDouble(), longitude.toDouble() )

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
                override fun onResponse(
                        call: Call<NotesMarkerOutputPost>,
                        response: Response<NotesMarkerOutputPost>
                ) {
                    if (response.isSuccessful) {
                        val c = response.body()!!

                        if (c.success) {
                            Toast.makeText(this@MapActivity, R.string.markercreatedlabel, Toast.LENGTH_SHORT).show()
                            val marker = mMap.addMarker(MarkerOptions().position(position).title(problema).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                            marker.tag = "${c.msg}-true"
                        }
                        else Toast.makeText(
                                this@MapActivity,
                                R.string.markerinsertfaillabel,
                                Toast.LENGTH_SHORT
                        ).show()
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

        mMap.setOnInfoWindowClickListener( object: GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(p0: Marker) {
                val intent = Intent(this@MapActivity, MapMarkerDetails::class.java)

                val split = TextUtils.split( "${p0.tag}", "-")
                var id: Int
                var editable: Boolean

                id = split[0].toInt()
                intent.putExtra(MapMarkerDetails.EXTRA_ID, id)

                if ( split.size > 1) {
                    editable = split[1].toBoolean()
                    intent.putExtra(MapMarkerDetails.EXTRA_EDITABLE, editable)
                }

                intent.putExtra(MapMarkerDetails.EXTRA_LATITUDE, lastLocation.latitude.toString())
                intent.putExtra(MapMarkerDetails.EXTRA_LONGITUDE, lastLocation.longitude.toString())

                startActivity(intent)
            }
        })
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
            R.id.filter_type -> {
                mMap.clear()
                load_marker("Acidente", null)
                true
            }
            R.id.filter_distance -> {
                mMap.clear()
                load_marker(null, 5000)
                true
            }
            R.id.show_markers -> {
                mMap.clear()
                load_marker(null, null)
                true
            }
            R.id.logout_btn -> {
                val sharedPref: SharedPreferences = getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.automatic_login_check), false)
                    putString(getString(R.string.automatic_login_username), null)
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

    fun load_marker( type: String?, distance: Int? ) {
        val request = ServiceBuilder.buildService(NotesMarkerEndPoints::class.java)
        var call: Call<List<MapMarker>>
        if( type != null ) call = request.getNotesMarkerByType(type)
        else call = request.getNotesMarker()

        var position: LatLng

        call.enqueue(object : Callback<List<MapMarker>> {
            override fun onResponse(call: Call<List<MapMarker>>, response: Response<List<MapMarker>>) {
                if (response.isSuccessful) {
                    val c = response.body()

                    if (c != null) {
                        for (note in c) {
                            position = LatLng(note.latitude.toDouble(), note.longitude.toDouble())
                            val distanceOf = calculateDistance(lastLocation.latitude, lastLocation.longitude, position.latitude, position.longitude)

                            if( distance != null ) {
                                if (distanceOf < distance) {
                                    if ( TextUtils.equals( note.user.username, user) ) {
                                        val marker = mMap.addMarker(MarkerOptions().position(position).title(note.problema+" - "+ distanceOf).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                                        marker.tag = "${note.id}-true"
                                        note.tipo_problema.tipo
                                    }
                                    else {
                                        val marker = mMap.addMarker(MarkerOptions().position(position).title(note.problema+" - "+ distanceOf))
                                        marker.tag = "${note.id}"
                                    }
                                }
                            }
                            else {
                                if ( TextUtils.equals( note.user.username, user) ) {
                                    val marker = mMap.addMarker(MarkerOptions().position(position).title(note.problema+" - "+ distanceOf).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                                    marker.tag = "${note.id}-true"
                                    note.tipo_problema.tipo
                                }
                                else {
                                    val marker = mMap.addMarker(MarkerOptions().position(position).title(note.problema+" - "+ distanceOf))
                                    marker.tag = "${note.id}"
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<MapMarker>>, t: Throwable) {
                Toast.makeText(this@MapActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun new_marker() {
        if ( ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        else {
            val intent = Intent(this@MapActivity, AddEditMapMarker::class.java)


            intent.putExtra(AddEditMapMarker.EXTRA_LATITUDE, lastLocation.latitude.toString())
            intent.putExtra(AddEditMapMarker.EXTRA_LONGITUDE, lastLocation.longitude.toString())
            intent.putExtra(AddEditMapMarker.EXTRA_USERNAME, user)
            startActivityForResult(intent, ADD_MAP_MARKER_REQUEST_CODE)
        }
    }

    // TEST

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
                    Log.d(
                            "****LOCATION****",
                            "New Location Received - " + loc.latitude + " -" + loc.longitude
                    )
                }
            }
        }
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
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

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
            )
        }

        startLocationUpdates()
        Log.d("****LOCATION****", "onResume - startLocationUpdates")
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopLocationUpdates()
        Log.d("****LOCATION****", "onPause - removeLocationUpdates")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if( event.sensor.type == Sensor.TYPE_ACCELEROMETER ) {
            val xChange: Float = history[0] - event.values[0]
            val yChange: Float = history[1] - event.values[1]

            history[0] = event.values[0]
            history[1] = event.values[1]

            if (xChange > 9) {
                direction = "E"
                Toast.makeText(this@MapActivity, "$direction", Toast.LENGTH_SHORT).show()
                Log.d("SENSORS", "onSensorChanged: TYPE_ACCELEROMETER: $direction | $xChange | $yChange")
            }
            else if (xChange < -9) {
                direction = "O"
                Toast.makeText(this@MapActivity, "$direction", Toast.LENGTH_SHORT).show()
                Log.d("SENSORS", "onSensorChanged: TYPE_ACCELEROMETER: $direction | $xChange | $yChange")
            }
            else if (yChange > 5) {
                direction = "N"
                Toast.makeText(this@MapActivity, "$direction", Toast.LENGTH_SHORT).show()
                Log.d("SENSORS", "onSensorChanged: TYPE_ACCELEROMETER: $direction | $xChange | $yChange")
            }
            else if (yChange < -5) {
                direction = "S"
                Toast.makeText(this@MapActivity, "$direction", Toast.LENGTH_SHORT).show()
                Log.d("SENSORS", "onSensorChanged: TYPE_ACCELEROMETER: $direction | $xChange | $yChange")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}