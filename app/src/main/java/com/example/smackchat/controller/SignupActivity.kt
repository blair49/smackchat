package com.example.smackchat.controller

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackchat.R
import com.example.smackchat.services.AuthService
import com.example.smackchat.utilities.BROADCAST_USER_DATA_CHANGE
import com.example.smackchat.utilities.MIN_PASSWORD_LENGTH
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*

class SignupActivity : AppCompatActivity() {

    private var userAvatar: String = "profileDefault"
    private var avatarBgColor: String = "[0.5,0.5,0.5,1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        signupProgressBar.visibility = View.INVISIBLE

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
        val userName = signupUserNameText.text.toString()
        val email = signupEmailText.text.toString()
        val password = signupPasswordText.text.toString()

        if(userName.isEmpty()){
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        } else if(email.isEmpty()){
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        } else if(password.length < MIN_PASSWORD_LENGTH){
            Toast.makeText(this, "Password should be atleast 6 characters",
                Toast.LENGTH_SHORT).show()
            return
        }

        showProgressSpinner(true)
        AuthService.registerUser(email, password){ registrationSuccessful ->
            if(registrationSuccessful){
                AuthService.loginUser(email, password){ loginSuccessful ->
                    if(loginSuccessful){
                        AuthService.addUser(userName, email, userAvatar, avatarBgColor){ userAdded ->
                            if(userAdded){
                                Toast.makeText(this, "User Created successfully",
                                    Toast.LENGTH_SHORT ).show()
                                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                showProgressSpinner(false)
                                finish()
                            }
                            else{
                                errorToast()
                            }
                        }
                    }
                    else{
                        errorToast()
                    }
                }

            }
            else{
                errorToast()
            }
        }
    }

    private fun errorToast(){
        Toast.makeText(this, "Something went wrong, please try again",
            Toast.LENGTH_LONG).show()
        showProgressSpinner(false)
    }

    fun showProgressSpinner(show : Boolean){
        if (show) {
            signupProgressBar.visibility = View.VISIBLE
        }
        else {
            signupProgressBar.visibility = View.INVISIBLE
        }
        //Enable buttons if not showing progress spinner
        signupBtn.isEnabled = !show
        generateBgColorBtn.isEnabled = !show
        signupAvatarImage.isEnabled = !show
    }
}
