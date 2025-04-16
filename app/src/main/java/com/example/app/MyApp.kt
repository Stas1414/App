package com.example.app

import android.app.Application
import android.util.Log
import com.example.app.utils.SessionManager

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(applicationContext)
        Log.i("MyApplication", "SessionManager initialized.")
    }
}