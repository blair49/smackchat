package com.blairfernandes.smackchat.controller

import android.app.Application
import com.blairfernandes.smackchat.utilities.SharedPrefs

class App: Application() {

    companion object{
        lateinit var prefs : SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}