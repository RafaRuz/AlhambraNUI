package com.example.alhambranui

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate

class HelpActivity : AppCompatActivity(), SensorEventListener {

    // Sensor variables to work with the light sensor
    var sensor : Sensor? = null
    var sensorManager : SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        if( MainActivity.insignias[0] == true){
            var txtResult = findViewById(R.id.insignia1ImageView) as ImageView
            txtResult.setAlpha(1f)
        }
        if( MainActivity.insignias[1] == true){
            var txtResult = findViewById(R.id.insignia2ImageView) as ImageView
            txtResult.setAlpha(1f)
        }
        if( MainActivity.insignias[2] == true){
            var txtResult = findViewById(R.id.insignia3ImageView) as ImageView
            txtResult.setAlpha(1f)
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

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

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        if( MainActivity.insignias[0] == true){
            var txtResult = findViewById(R.id.insignia1ImageView) as ImageView
            txtResult.setAlpha(255)
        }
        if( MainActivity.insignias[1] == true){
            var txtResult = findViewById(R.id.insignia2ImageView) as ImageView
            txtResult.setAlpha(255)
        }
        if( MainActivity.insignias[2] == true){
            var txtResult = findViewById(R.id.insignia3ImageView) as ImageView
            txtResult.setAlpha(255)
        }
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}
