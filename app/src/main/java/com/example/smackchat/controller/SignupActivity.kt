package com.example.smackchat.controller

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.smackchat.R
import com.example.smackchat.services.AuthService
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*

class SignupActivity : AppCompatActivity() {

    var userAvatar: String = "profileDefault"
    var avatarBgColor: String = "[0.5,0.5,0.5,1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
    }

    fun generateUserAvatar(view:View){
        val random = Random()
        val avatarColor = random.nextInt(2)
        val avatar = random.nextInt(28)
        if(avatarColor == 0){
            userAvatar = "light$avatar"
        } else {
            userAvatar = "dark$avatar"
        }
        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        signupAvatarImage.setImageResource(resourceId)

    }

    fun generateBgColorBtnClicked(view: View){
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)
        signupAvatarImage.setBackgroundColor(Color.rgb(r,g,b))
        val savedR = r.toDouble()/255
        val savedG = g.toDouble()/255
        val savedB = b.toDouble()/255
        avatarBgColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun signupBtnClicked(view: View){
        AuthService.registerUser(this, "j@j.com", "123456"){complete ->
            if(complete){
                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT ).show();
            }
            else{
                Toast.makeText(this, "Registeration failed!", Toast.LENGTH_SHORT ).show();
            }
        }
    }
}
