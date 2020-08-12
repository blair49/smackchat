package com.example.smackchat.services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.example.smackchat.controller.App
import com.example.smackchat.model.Channel
import com.example.smackchat.model.Message
import com.example.smackchat.utilities.URL_GET_CHANNELS
import com.example.smackchat.utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit){
        val channelRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS,
                                    null, Response.Listener { response ->
            clearChannels()
            try {
                for (x in 0 until response.length()){
                    val channel = response.getJSONObject(x)
                    val channelName = channel.getString("name")
                    val channelDescription = channel.getString("description")
                    val channelId = channel.getString("_id")

                    val newChannel = Channel(channelName, channelDescription, channelId)
                    this.channels.add(newChannel)
                }
                complete(true)

            } catch (e: JSONException){
                Log.d("JSON", "EXC:" + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(channelRequest)
    }

    fun getMessages(context: Context, channelId:String, complete: (Boolean) -> Unit){
        val url = "$URL_GET_MESSAGES$channelId"

        val messageRequest = object : JsonArrayRequest(Method.GET, url, null,
            Response.Listener { response ->
                clearMessages()
                try {
                    for(x in 0 until response.length()){
                        val newMessage = response.getJSONObject(x)
                        val messageBody = newMessage.getString("messageBody")
                        val channelId = newMessage.getString("channelId")
                        val id = newMessage.getString("_id")
                        val username = newMessage.getString("userName")
                        val userAvatar = newMessage.getString("userAvatar")
                        val userAvatarBgColor = newMessage.getString("userAvatarColor")
                        val timeStamp = newMessage.getString("timeStamp")
                        val userProfilePicture = newMessage.getString("userProfilePicture")
                        //get and save profile picture of sender
                        AuthService.getPhoto(context, userProfilePicture){}
                        //Create a message object and add it to message list
                        val message = Message(messageBody, username, channelId, userAvatar,
                                        userAvatarBgColor, id, timeStamp, userProfilePicture)
                        this.messages.add(message)
                    }
                    complete(true)

                } catch (e: JSONException){
                    Log.d("JSON", "EXC:" + e.localizedMessage)
                    complete(false)
                }

        }, Response.ErrorListener {
            Log.d("ERROR", "Could not retrieve messages")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        App.prefs.requestQueue.add(messageRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChannels(){
        channels.clear()
    }

}