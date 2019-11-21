package com.example.alhambranui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate


var nightModeEnabled = false
var lightSensorThreshold = 6

// Variables globales para comprobar los niveles completados en la actividad "Mosaicos"
var completados : BooleanArray = BooleanArray(3)
var completados_hard : BooleanArray = BooleanArray(3)

class MainActivity : AppCompatActivity(), SensorEventListener {

    // Sensor variables to work with the light sensor
    var sensor : Sensor? = null
    var sensorManager : SensorManager? = null
    companion object{
        var insignias : BooleanArray = BooleanArray(3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // pantalla en vertical siempre
    }

    /** Called when the user taps "Comenzar visita" button */
    fun comenzarVisita(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    /** Called when the user taps "Insignias" button */
    fun help(view: View) {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    /** Detect light sensor changes */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_LIGHT){
            if (event.values[0] < lightSensorThreshold && !nightModeEnabled){
                nightModeEnabled = true

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                finish()

                startActivity(Intent(applicationContext, this::class.java))
            }
            else if (event.values[0] > lightSensorThreshold && nightModeEnabled){
                nightModeEnabled = false

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                finish()
                startActivity(Intent(applicationContext, this::class.java))
            }
        }

    }


    fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        hideSystemUI()
    }
}
