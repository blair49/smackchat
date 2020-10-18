package com.blairfernandes.smackchat.services

import android.graphics.Color
import com.blairfernandes.smackchat.controller.App
import java.util.*

object UserDataService {
    var id =""
    var avatarBgColor = ""
    var avatar = ""
    var email = ""
    var userName = ""
    var profilePicture: String? = null

    fun getAvatarBgColor(avatarBgColor:String) : Int {
        val strippedColor = avatarBgColor.replace("[", "")
                            .replace("]", "").replace(",", "")

        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor)
        if (scanner.hasNext()){
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }
        return Color.rgb(r, g, b)
    }

    fun logout(){
        id =""
        avatarBgColor = ""
        avatar = ""
        email = ""
        userName = ""
        profilePicture = null

        App.prefs.userEmail = ""
        App.prefs.authToken = ""
        App.prefs.isLoggedIn = false

        MessageService.clearChannels()
        MessageService.clearMessages()
    }
}