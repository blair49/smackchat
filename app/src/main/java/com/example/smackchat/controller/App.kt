package com.example.smackchat.controller

import android.app.Application
import com.example.smackchat.utilities.SharedPrefs

class App: Application() {

    companion object{
        lateinit var prefs : SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}