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


class Mos1HardActivity : AppCompatActivity(), SensorEventListener {

    var sensor : Sensor? = null
    var sensorManager : SensorManager? = null
    var handler: Handler? = null
    var lightSensorThreshold=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mos1hard)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Si el nivel no ha sido completado todavía, se darán 1.5 segundos
        // para ver la imagen. Tras ello la imagen desaparecerá.
        if(!completados_hard[0]) {
            handler = Handler()

            handler!!.postDelayed(Runnable {
                var layout =
                    findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mos1_layout)
                layout.setBackgroundColor(getResources().getColor(R.color.backgroundColor))
            }, 1500)
        }

        var txtResult = findViewById(R.id.resultado_gesto) as TextView

        // Si el nivel ya ha sido completado previamente, hacemos que
        // se le muestre dicha información al usuario
        if(completados_hard[0]){
            txtResult.setVisibility(View.VISIBLE)
            findViewById<Button>(R.id.boton_atras).setVisibility(View.VISIBLE)
        }

        // Cargamos la librería de gestos, realizada con "Gesture Builder"
        var lib = GestureLibraries.fromRawResource(this, R.raw.gesture_mos1)
        if (!lib.load()) {
            finish()
        }

        // Comprobamos si el dibujo realizado se asemeja a alguno de los almacenados
        // en la librería de gestos, y si es así, mostramos que se ha superado el nivel,
        // actualizamos la variable global, completados_hard[0] = true y mostramos de nuevo
        // la imagen del mosaico que previamente habíamos eliminado
        // Si se falla en el dibujo, se muestra un toast avisando de ello
        val gesture = findViewById(R.id.gesture_overlay_view) as GestureOverlayView
        gesture.addOnGesturePerformedListener { overlay, gesture ->
            val predictionArrayList = lib.recognize(gesture)
            var encontrado : Boolean = false
            for (prediction in predictionArrayList) {
                if (prediction.score > 8) {
                    txtResult.setVisibility(View.VISIBLE)
                    findViewById<Button>(R.id.boton_atras).setVisibility(View.VISIBLE)
                    var layout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mos1_layout)
                    layout.setBackgroundResource(R.drawable.mos1)
                    completados_hard[0]=true
                    encontrado=true
                    break
                }
            }
            if(!encontrado) Toast.makeText(this, "Prueba otra vez :)", Toast.LENGTH_SHORT).show()
        }
    }

    /** Called when the user taps "Atrás" button */
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
