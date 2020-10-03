package com.example.smackchat.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.smackchat.R
import com.example.smackchat.controller.App
import com.example.smackchat.utilities.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*

object AuthService {
//    var userEmail = ""
//    var isLoggedIn = false
//    var authToken = ""

    fun registerUser(name: String, email: String, avatar: String, avatarBgColor: String,
                     password: String, complete: (Boolean) -> Unit){
        val url = URL_REGISTER

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatar)
        jsonBody.put("avatarColor", avatarBgColor)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : JsonObjectRequest(Method.POST, url, null,
            Response.Listener { response ->
                try {
                    UserDataService.id = response.getString("id")
                    complete(true)
                }catch (e : JSONException){
                    Log.d("JSON", "EXC : "+ e.localizedMessage)
                    complete(false)
                }
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
        registerRequest.retryPolicy = DefaultRetryPolicy(
            REQUEST_TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.prefs.requestQueue.add(registerRequest)
    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit){
        val url = URL_LOGIN

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, url, null,
            Response.Listener { response ->
            //Handle response
            try {
                App.prefs.userEmail = email
                App.prefs.authToken = response.getString("token")
                App.prefs.isLoggedIn = true
                complete(true)
            } catch (e : JSONException){
                Log.d("JSON", "EXC : "+ e.localizedMessage)
                App.prefs.isLoggedIn = false
                complete(false)
            }


        }, Response.ErrorListener { error ->
            //Handle Error
            Log.d("ERROR", "Could not login user $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        //Set custom timeout to prevent request timeout due to heroku startup delay
        loginRequest.retryPolicy = DefaultRetryPolicy(
            REQUEST_TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        App.prefs.requestQueue.add(loginRequest)
    }

    fun addUser(name: String, email: String, avatar: String, avatarBgColor: String,
                complete: (Boolean) -> Unit){
        val url = URL_ADD_USER

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatar)
        jsonBody.put("avatarColor", avatarBgColor)
        val requestBody = jsonBody.toString()

        val addUserRequest = object : JsonObjectRequest(Method.POST, url, null,
            Response.Listener {response ->
            try {
                UserDataService.userName = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatar = response.getString("avatarName")
                UserDataService.avatarBgColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")
                complete(true)
            } catch (e: JSONException){
                Log.d("JSON", "EXC : "+ e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not add user $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.prefs.authToken}"
                return headers
            }
        }

        App.prefs.requestQueue.add(addUserRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit){
        val url = "${URL_FIND_USER}${App.prefs.userEmail}"
        val findRequest = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener { response ->
            try {
                UserDataService.userName = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatar = response.getString("avatarName")
                UserDataService.avatarBgColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")
                UserDataService.profilePicture = response.getString("profilePicture")

                //Get photo with file name received
                UserDataService.profilePicture?.let {profilePicture ->
                    getPhoto(context, profilePicture){}
                }

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)
            } catch (e: JSONException){
                Log.d("JSON", "EXC : "+ e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener {error ->
            Log.d("ERROR", "Could not find user: $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.prefs.authToken}"
                return headers
            }
        }

        App.prefs.requestQueue.add(findRequest)
    }

    fun uploadPhoto(context: Context, imageBitmap: Bitmap, fileName: String, complete: (Boolean) -> Unit){
        val url = "${URL_UPLOAD_PHOTO}${UserDataService.id}/photo"

        val uploadRequest = object : VolleyFileUploadRequest(Method.PUT, url,
            Response.Listener { response ->
            try {
                Log.d("RES", response.toString())
                UserDataService.profilePicture = response.getString("fileName")

                UserDataService.profilePicture?.let { profilePicture ->
                    saveReceivedImage(imageBitmap, profilePicture, context)
                }

                complete(true)
            } catch (e: JSONException){
                Log.d("JSON", "EXC : "+ e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not upload photo: ${error.message}")
            complete(false)
        }){
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()
                params["file"] = FileDataPart(fileName, getFileDataFromDrawable(imageBitmap),
                    "image/jpeg" )
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.prefs.authToken}"
                return headers
            }
        }

        App.prefs.requestQueue.add(uploadRequest)
    }

    fun getPhoto(context: Context, photoFileName: String, complete: (Boolean) -> Unit){
        val url = "${URL_GET_PHOTO}$photoFileName"

        val getPhotoRequest = object : ImageRequest(url, Response.Listener { imageBitmap:Bitmap ->
            saveReceivedImage(imageBitmap, photoFileName, context)
            complete(true)
        }, 0, 0,ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
            Response.ErrorListener {error ->
                Log.d("ERROR", "Error getting photo: $error")
                complete(false)
        }){

        }

        App.prefs.requestQueue.add(getPhotoRequest)
    }

    private fun getFileDataFromDrawable(bitmap: Bitmap):ByteArray{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun saveReceivedImage(bitmap: Bitmap, fileName: String, context: Context){
        try {
            val relativePath = "${R.string.app_name}${File.separator}Images"
            val path = File(context.filesDir, relativePath)
            if(!path.exists()){
                path.mkdirs()
            }
            val outFile = File(path, fileName)
            val outputStream = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            App.prefs.profilePicturePath = relativePath

        } catch (e: FileNotFoundException){
            Log.e("EXC", e.localizedMessage)
        } catch (e: IOException){
            Log.e("EXC", e.localizedMessage)
        }
    }
}