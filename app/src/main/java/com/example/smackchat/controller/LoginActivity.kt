package com.example.smackchat.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackchat.R
import com.example.smackchat.services.AuthService
import com.example.smackchat.utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginProgressBar.visibility = View.INVISIBLE
    }

    fun loginLoginBtnClicked(view: View){
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()
        if(email.isEmpty()){
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        } else if (password.isEmpty()){
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        showProgressSpinner(true)
        hideKeyboard()
        AuthService.loginUser(this, email, password){ loginSuccessful ->
            if (loginSuccessful){
                AuthService.findUserByEmail(this){ userFound->
                    if(userFound){
                        val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                        showProgressSpinner(false)
                        finish()
                    } else{
                        errorToast()
                    }
                }
            } else {
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
            loginProgressBar.visibility = View.VISIBLE
        }
        else {
            loginProgressBar.visibility = View.INVISIBLE
        }
        //Enable buttons if not showing progress spinner
        loginSignupBtn.isEnabled = !show
        loginLoginBtn.isEnabled = !show
    }

    fun loginSignupBtnClicked(view: View){
        val signupIntent = Intent(this, SignupActivity::class.java)
        startActivity(signupIntent)
        finish()
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

    }
}
