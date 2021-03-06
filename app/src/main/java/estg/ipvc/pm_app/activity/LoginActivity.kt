package estg.ipvc.pm_app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import estg.ipvc.pm_app.API.LoginEndPoints
import estg.ipvc.pm_app.API.LoginOutputPost
import estg.ipvc.pm_app.API.ServiceBuilder
import estg.ipvc.pm_app.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

class LoginActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager

    private lateinit var usernameEditTextView: EditText
    private lateinit var passwordEditTextView: EditText
    private lateinit var submit_login_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        usernameEditTextView = findViewById(R.id.login_username)
        passwordEditTextView = findViewById(R.id.login_password)
        submit_login_button = findViewById(R.id.submit_login_button)

        val sharedPref: SharedPreferences = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE )

        val automatic_login_check = sharedPref.getBoolean(getString(R.string.automatic_login_check), false)
        Log.d("SP_AutoLoginCheck", "$automatic_login_check")

        if( automatic_login_check ) {
                val intent = Intent(this@LoginActivity, MapActivity::class.java)
                startActivity(intent)
                finish()
        }
    }

    fun login( view: View ) {

        val username = usernameEditTextView.text.toString()
        val password = passwordEditTextView.text.toString()

        if ( TextUtils.isEmpty(username) ) {
            Toast.makeText(this, R.string.fieldusernameemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( TextUtils.isEmpty(password) ) {
            Toast.makeText(this, R.string.fieldpasswordemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else {

            val request = ServiceBuilder.buildService(LoginEndPoints::class.java)
            val call = request.postLoginTest(
                    username,
                    password.sha256()
            )

            call.enqueue(object : Callback<LoginOutputPost> {
                override fun onResponse(
                    call: Call<LoginOutputPost>,
                    response: Response<LoginOutputPost>
                ) {
                    if (response.isSuccessful) {
                        val c: LoginOutputPost = response.body()!!

                        if (c.success) {
                            Toast.makeText(
                                this@LoginActivity,
                                R.string.logincorrectlabel,
                                Toast.LENGTH_SHORT
                            ).show()

                            val sharedPref: SharedPreferences = getSharedPreferences(
                                    getString(R.string.preference_file_key), Context.MODE_PRIVATE )
                            with ( sharedPref.edit() ) {
                                putBoolean(getString(R.string.automatic_login_check), true)
                                putString(getString(R.string.automatic_login_username), username )
                                commit()
                            }

                            val intent = Intent(this@LoginActivity, MapActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else Toast.makeText(
                            this@LoginActivity,
                            R.string.loginincorrectlabel,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginOutputPost>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun register( view: View ) {

        val username = usernameEditTextView.text.toString()
        val password = passwordEditTextView.text.toString()

        if ( TextUtils.isEmpty(username) ) {
            Toast.makeText(this, R.string.fieldusernameemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else if ( TextUtils.isEmpty(password) ) {
            Toast.makeText(this, R.string.fieldpasswordemptylabel, Toast.LENGTH_LONG).show()
            return
        }
        else {

            val request = ServiceBuilder.buildService(LoginEndPoints::class.java)
            val call = request.postRegisterTest(
                    username,
                    password.sha256()
            )

            call.enqueue(object : Callback<LoginOutputPost> {
                override fun onResponse(
                        call: Call<LoginOutputPost>,
                        response: Response<LoginOutputPost>
                ) {
                    if (response.isSuccessful) {
                        val c: LoginOutputPost = response.body()!!

                        if (c.success) {
                            Toast.makeText(
                                    this@LoginActivity,
                                    R.string.registercorrectlabel,
                                    Toast.LENGTH_SHORT
                            ).show()
                        } else Toast.makeText(
                                this@LoginActivity,
                                R.string.registerincorrectlabel,
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginOutputPost>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun show_notes( view: View ) {
        val intent = Intent( this, NoteActivity::class.java )
        startActivity(intent)
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
            val layout = findViewById<ConstraintLayout>(R.id.login_layout)
            val login_username = findViewById<EditText>(R.id.login_username)
            val login_password = findViewById<EditText>(R.id.login_password)

            Log.d("SENSORS", "onSensorChanged: TYPE_LIGHT: ${event.values[0]}")
            if (event.values[0] > 5000) {
                layout.setBackgroundColor(Color.parseColor("#000000"))
                login_username.setTextColor(Color.parseColor("#FFFFFF"))
                login_username.setHintTextColor(Color.parseColor("#FFFFFF"))
                login_password.setTextColor(Color.parseColor("#FFFFFF"))
                login_password.setHintTextColor(Color.parseColor("#FFFFFF"))
            }
            else {
                layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                login_username.setTextColor(Color.parseColor("#000000"))
                login_username.setHintTextColor(Color.parseColor("#000000"))
                login_password.setTextColor(Color.parseColor("#000000"))
                login_password.setHintTextColor(Color.parseColor("#000000"))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    fun String.sha256(): String {
        return hashString(this, "SHA-256")
    }

    private fun hashString(input: String, algorithm: String): String {
        return MessageDigest
            .getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}