package com.blairfernandes.smackchat.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blairfernandes.smackchat.R
import com.blairfernandes.smackchat.controller.App
import com.blairfernandes.smackchat.model.Message
import com.blairfernandes.smackchat.services.UserDataService
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageAdapter(val context: Context, val messages: ArrayList<Message>)
                    : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImage = itemView.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView.findViewById<TextView>(R.id.messageDateTimeText)
        val userName = itemView.findViewById<TextView>(R.id.messageUsernameText)
        val messageBody = itemView.findViewById<TextView>(R.id.messageBodyText)

        fun bindMessage(context: Context, message: Message){
            val resourceId = context.resources.getIdentifier(message.userAvatar,
                        "drawable", context.packageName)
            message.userProfilePicture?.let {userProfilePicture ->
                val imageFilePath = "${App.prefs.profilePicturePath}${File.separator}$userProfilePicture"
                val imageFile = File(context.filesDir, imageFilePath)

                if(imageFile.exists()){
                    val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    userImage.setImageBitmap(imageBitmap)
                } else{
                    Log.d("ERROR", "Error getting photo")
                    userImage.setImageResource(resourceId)
                    userImage.setBackgroundColor(UserDataService.getAvatarBgColor(message.userAvatarBgColor))
                }
            }

            timeStamp.text = getDateString(message.timeStamp)
            userName.text = message.username
            messageBody.text = message.message
        }

        private fun getDateString(isoDateString: String) : String{
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()
            try {
                convertedDate = isoFormatter.parse(isoDateString)
            } catch (e: ParseException){
                Log.d("PARSE", "Cannot parse date")
            }
            val resultDateString = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            return resultDateString.format(convertedDate)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMessage(context, messages[position])
    }
}