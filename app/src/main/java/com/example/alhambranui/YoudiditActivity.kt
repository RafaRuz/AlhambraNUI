package com.example.alhambranui

import android.app.Activity
import android.content.Intent
import android.R
import android.os.Bundle
import android.os.Handler


class YoudiditActivity : Activity() {

    var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.alhambranui.R.layout.activity_youdidit)

        handler = Handler()
        handler!!.postDelayed(Runnable {
            finish()
        }, 3000)

    }
}