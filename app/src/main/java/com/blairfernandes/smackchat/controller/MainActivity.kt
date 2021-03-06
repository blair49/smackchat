package com.blairfernandes.smackchat.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blairfernandes.smackchat.R
import com.blairfernandes.smackchat.adapters.MessageAdapter
import com.blairfernandes.smackchat.model.Channel
import com.blairfernandes.smackchat.model.Message
import com.blairfernandes.smackchat.services.AuthService
import com.blairfernandes.smackchat.services.MessageService
import com.blairfernandes.smackchat.services.UserDataService
import com.blairfernandes.smackchat.utilities.BROADCAST_USER_DATA_CHANGE
import com.blairfernandes.smackchat.utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel:Channel? = null
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle : ActionBarDrawerToggle

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar= findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle = setupDrawerToggle()
        // Setup toggle to display hamburger icon with nice animation
        drawerToggle.setDrawerIndicatorEnabled(true)
        drawerToggle.syncState()
        // Tie DrawerLayout events to the ActionBarToggle
        drawer_layout.addDrawerListener(drawerToggle)

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)
        setupAdapters()
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE))

        //Load user data if previously logged in
        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){ foundUser ->
                if(!foundUser) {
                    UserDataService.logout()
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                }
            }
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        channel_list.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this, drawer_layout, toolbar,
                                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)

        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            if(App.prefs.isLoggedIn){

                userNameNavHeader.text = UserDataService.userName
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatar, "drawable"
                    , packageName)

                UserDataService.profilePicture?.let {profilePicture ->
                    val imageFilePath = "${App.prefs.profilePicturePath}${File.separator}$profilePicture"
                    val imageFile = File(context.filesDir, imageFilePath)

                    if(imageFile.exists()){
                        val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                        userImageNavHeader.setImageBitmap(imageBitmap)
                    } else{
                        Log.d("ERROR", "Error getting photo")
                        userImageNavHeader.setImageResource(resourceId)
                        userImageNavHeader.setBackgroundColor(UserDataService
                            .getAvatarBgColor(UserDataService.avatarBgColor))
                    }
                }

                loginButtonNavHeader.text = "Logout"
                refreshChannels()
            }
        }
    }

    private fun refreshChannels(){
        currentChannelName.text = "No channels found!"
        showProgressBar(true)
        MessageService.getChannels { foundChannels ->
            showProgressBar(false)
            if(foundChannels){
                if(MessageService.channels.count() > 0){
                    selectedChannel = MessageService.channels[0]
                    channelAdapter.notifyDataSetChanged()
                    updateWithChannel()
                }
            }
        }
    }

    private fun updateWithChannel(){
        currentChannelName.text = selectedChannel?.toString()
        //download messages for channel
        if(selectedChannel != null){
            showProgressBar(true)
            MessageService.getMessages(this, selectedChannel!!.id){ foundMessages ->
                if (foundMessages){
                    messageAdapter.notifyDataSetChanged()
                    if (messageAdapter.itemCount > 0){
                        //scroll to the most recent message
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
                showProgressBar(false)
            }
        }
    }

    fun loginBtnNavClicked(view: View){
        if(App.prefs.isLoggedIn){
            //logout
            UserDataService.logout()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "Login"
            selectedChannel=null
            currentChannelName.text = "Please login"
            messageAdapter.notifyDataSetChanged()
            channelAdapter.notifyDataSetChanged()
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun addChannelClicked(view: View){
        if(App.prefs.isLoggedIn){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                .setPositiveButton("Add"){ dialogInterface, i ->  
                    val channelNameText = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val channelDescriptionText = dialogView.findViewById<EditText>(R.id.addChannelDescriptionText)
                    val channelName = channelNameText.text.toString()
                    val channelDescription = channelDescriptionText.text.toString()
                    //create channel with channel name and description
                    socket.emit("newChannel", channelName, channelDescription)
                }
                .setNegativeButton("Cancel"){ dialogInterface, i ->
                    //close the dialog

                }
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if(App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)
                if(MessageService.channels.count() > 0){
                    selectedChannel = MessageService.channels[0]
                    channelAdapter.notifyDataSetChanged()
                    updateWithChannel()
                }
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if(App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String
                if(channelId.equals(selectedChannel?.id)) {
                    val messageBody = args[0] as String

                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarBgColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String
                    val userProfilePicture = args[8] as String
                    AuthService.getPhoto(this, userProfilePicture){}
                    val newMessage = Message(
                        messageBody, userName, channelId, userAvatar,
                        userAvatarBgColor, id, timeStamp, userProfilePicture
                    )
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }

    fun sendMsgBtnClicked(view: View){
        if(App.prefs.isLoggedIn && messageEditText.text.isNotEmpty() && selectedChannel != null){
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageEditText.text.toString(), userId, channelId,
                UserDataService.userName, UserDataService.avatar, UserDataService.avatarBgColor,
                UserDataService.profilePicture)
            messageEditText.text.clear()
        }
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }

    }

    private fun showProgressBar(show: Boolean){
        if(show){
            loadingProgressBar.visibility = View.VISIBLE
        } else{
            loadingProgressBar.visibility = View.INVISIBLE
        }
        //Enable buttons and text view if not showing progress bar
        messageEditText.isEnabled = !show
        sendMsgBtn.isEnabled = !show
    }
}
