package com.example.alhambranui

import android.content.Context
import android.content.Intent
import android.gesture.GestureLibraries
import android.gesture.GestureOverlayView
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate


class Mos3HardActivity : AppCompatActivity(), SensorEventListener {

    // Código similar a Mos1HardActivity.kt
    // Comentado en dicho archivo

    var sensor : Sensor? = null
    var sensorManager : SensorManager? = null
    var handler: Handler? = null
    var lightSensorThreshold=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mos3hard)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        if(!completados_hard[2]) {
            handler = Handler()

            handler!!.postDelayed(Runnable {
                var layout =
                    findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mos3_layout)
                layout.setBackgroundColor(getResources().getColor(R.color.backgroundColor))
            }, 1500)
        }

        var txtResult = findViewById(R.id.resultado_gesto) as TextView

        if(completados_hard[2]){
            txtResult.setVisibility(View.VISIBLE)
            findViewById<Button>(R.id.boton_atras).setVisibility(View.VISIBLE)
        }

        var lib = GestureLibraries.fromRawResource(this, R.raw.gesture_mos3)

        if (!lib.load()) {
            finish()
        }

        val gesture = findViewById(R.id.gesture_overlay_view) as GestureOverlayView
        gesture.addOnGesturePerformedListener { overlay, gesture ->
            val predictionArrayList = lib.recognize(gesture)
            var encontrado : Boolean = false
            for (prediction in predictionArrayList) {
                if (prediction.score > 3) {
                    txtResult.setVisibility(View.VISIBLE)
                    findViewById<Button>(R.id.boton_atras).setVisibility(View.VISIBLE)
                    var layout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mos3_layout)
                    layout.setBackgroundResource(R.drawable.mos3)
                    completados_hard[2]=true
                    encontrado=true
                    break
                }
            }
            if(!encontrado) Toast.makeText(this, "Prueba otra vez :)", Toast.LENGTH_SHORT).show()
        }
    }

    fun atras(view: View) {
        val intent = Intent(this, MosaicosActivity::class.java)
        startActivity(intent)
        finish()
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
