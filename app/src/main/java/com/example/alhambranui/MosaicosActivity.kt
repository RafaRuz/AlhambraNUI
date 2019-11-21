package com.example.alhambranui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MosaicosActivity : AppCompatActivity(), SensorEventListener {
    // Sensor variables to work with the light sensor
    var lightSensor : Sensor? = null
    var shake_active : Boolean = false
    var lightSensorThreshold=0

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null

    internal var fab: FloatingActionButton? = null

    /** En esta función se crea y modifica la vista para acceder a los distintos mosaicos **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mosaicos)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        lightSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        fab = findViewById(R.id.fab_carlosv)

        // Diálogo de ayuda
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(
            "Listo"
        ) { dialog, id ->
            dialog.cancel()
            hideSystemUI()
        }
        builder.setMessage("Dibuja el contorno de los mosaicos para superar cada nivel. Una vez superado el nivel fácil, se desbloquerá el modo difícil.\n\nCompleta los tres mosaicos del nivel fácil para superar la actividad y agita para volver al mapa.")
        // Create the AlertDialog
        val dialog = builder.create()

        fab!!.setOnClickListener(View.OnClickListener { dialog.show() })

        // Comprobamos si todos los puzzles del nivel fácil han sido superados
        var todos_completados : Boolean=true
        for(nivel in completados) {
            if (nivel == false)
                todos_completados = false
        }

        // Si han sido completados, se muestra el modo difícil
        // y se da opción a agitar el teléfono para terminar la actividad
        if(todos_completados && !shake_active) {
            Toast.makeText(this, "Has completado el modo fácil! \n Agita para salir", Toast.LENGTH_LONG).show()

            // Modo difícil visibel
            findViewById<TextView>(R.id.mos_title2).setVisibility(View.VISIBLE)
            findViewById<LinearLayout>(R.id.hard).setVisibility(View.VISIBLE)

            shake_active = true
            mShakeDetector!!.setOnShakeListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

    }

    /** Called when the user taps "mos1" image button */
    fun mos1_easy(view: View) {
        val intent = Intent(this, Mos1Activity::class.java)
        startActivity(intent)
        // Cada vez que accedamos a un mosaico, debemos de finalizar la vista
        // de los distintos mosaicos para actualizar el valor de los ya superados
        finish()
    }

    /** Called when the user taps "mos2" image button */
    fun mos2_easy(view: View) {
        val intent = Intent(this, Mos2Activity::class.java)
        startActivity(intent)
        finish()
    }

    /** Called when the user taps "mos3" image button */
    fun mos3_easy(view: View) {
        val intent = Intent(this, Mos3Activity::class.java)
        startActivity(intent)
        finish()
    }

    /** Called when the user taps "mos1_hard" image button */
    fun mos1_hard(view: View) {
        val intent = Intent(this, Mos1HardActivity::class.java)
        startActivity(intent)
        finish()
    }

    /** Called when the user taps "mos2_hard" image button */
    fun mos2_hard(view: View) {
        val intent = Intent(this, Mos2HardActivity::class.java)
        startActivity(intent)
        finish()
    }

    /** Called when the user taps "mos3_hard" image button */
    fun mos3_hard(view: View) {
        val intent = Intent(this, Mos3HardActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    /** Detect light sensor changes */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            if (event.values[0] < lightSensorThreshold && !nightModeEnabled) {
                nightModeEnabled = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                val intent = Intent(this, MosaicosActivity::class.java)
                finish()
                startActivity(intent)
            } else if (event.values[0] > lightSensorThreshold && nightModeEnabled) {
                nightModeEnabled = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val intent = Intent(this, MosaicosActivity::class.java)
                finish()
                startActivity(intent)
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
        mSensorManager!!.unregisterListener(this)
        mSensorManager!!.unregisterListener(mShakeDetector)
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
        hideSystemUI()
    }

}