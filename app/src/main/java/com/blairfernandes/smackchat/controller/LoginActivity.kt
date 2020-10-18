package com.blairfernandes.smackchat.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.blairfernandes.smackchat.R
import com.blairfernandes.smackchat.services.AuthService
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
        val validEmail = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if(!validEmail){
            Toast.makeText(this, "Please enter a valid email id", Toast.LENGTH_SHORT).show()
            return
        } else if (password.isEmpty()){
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        showProgressSpinner(true)
        hideKeyboard()
        AuthService.loginUser(email, password){ loginSuccessful ->
            if (loginSuccessful){
                AuthService.findUserByEmail(this){ userFound->
                    if(userFound){
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
        showAlert()
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Kindly verify your email if you haven't already. " +
                    "Check your email inbox and spam for the verification email")
            .setPositiveButton("OK"){ _, _ ->
            }
            .show()
    }

    private fun showProgressSpinner(show : Boolean){
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

    private fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

    }
}
