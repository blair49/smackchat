package com.example.smackchat.services

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.smackchat.utilities.REQUEST_TIMEOUT
import com.example.smackchat.utilities.URL_LOGIN
import com.example.smackchat.utilities.URL_REGISTER
import org.json.JSONException
import org.json.JSONObject

object AuthService {
    var userEmail = ""
    var isLoggedIn = false
    var authToken = ""

    fun registerUser(context: Context, email:String, password:String, complete:(Boolean) -> Unit){
        val url = URL_REGISTER

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Request.Method.POST, url, Response.Listener {_->
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not register user $error")
            complete(false)
        } ){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        //Set custom timeout to prevent request timeout due to heroku startup delay
        registerRequest.setRetryPolicy(DefaultRetryPolicy(
            REQUEST_TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ))
        Volley.newRequestQueue(context).add(registerRequest)
    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit){
        val url = URL_LOGIN

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, url, null, Response.Listener { response ->
            //Handle response
            try {
                userEmail = response.getString("user")
                authToken = response.getString("token")
                isLoggedIn = true
                complete(isLoggedIn)
            } catch (e : JSONException){
                Log.d("JSON", "EXC : "+ e.localizedMessage)
                isLoggedIn = false
                complete(isLoggedIn)
            }


        }, Response.ErrorListener { error ->
            //Handle Error
            Log.d("ERROR", "Could not register user $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        Volley.newRequestQueue(context).add(loginRequest)
    }
}