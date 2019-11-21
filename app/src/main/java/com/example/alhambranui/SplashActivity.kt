package com.example.alhambranui

import android.app.Activity
import android.content.Intent
import android.R
import android.os.Bundle
import android.os.Handler


class SplashActivity : Activity() {

    var handler: Handler? = null

    /** Imagen que se muestra al comenzar la aplicacion **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.alhambranui.R.layout.activity_splash)

        // A los 3 segundos, se termina la actividad y se da paso a la pantalla principal
        handler = Handler()
        handler!!.postDelayed(Runnable {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

    }
}