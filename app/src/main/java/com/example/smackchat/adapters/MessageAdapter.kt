package com.example.smackchat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smackchat.R
import com.example.smackchat.model.Message
import com.example.smackchat.services.UserDataService

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
            userImage.setImageResource(resourceId)
            userImage.setBackgroundColor(UserDataService.getAvatarBgColor(message.userAvatarBgColor))
            timeStamp.text = message.timeStamp
            userName.text = message.username
            messageBody.text = message.message
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