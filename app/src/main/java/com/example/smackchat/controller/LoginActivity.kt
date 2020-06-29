package com.example.smackchat.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.smackchat.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginLoginBtnClicked(view: View){

    }

    fun loginSignupBtnClicked(view: View){
        val signupIntent = Intent(this, SignupActivity::class.java)
        startActivity(signupIntent)
        finish()
    }
}
