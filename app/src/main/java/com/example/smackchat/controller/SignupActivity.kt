package com.example.smackchat.controller

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smackchat.R
import com.example.smackchat.services.AuthService
import com.example.smackchat.utilities.BROADCAST_USER_DATA_CHANGE
import com.example.smackchat.utilities.MAX_IMAGE_FILE_SIZE
import com.example.smackchat.utilities.MIN_PASSWORD_LENGTH
import com.example.smackchat.utilities.SELECT_PROFILE_IMAGE
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.InputStream
import java.util.*

class SignupActivity : AppCompatActivity() {

    private var userAvatar: String = "profileDefault"
    private var avatarBgColor: String = "[0.5, 0.5, 0.5, 1]"
    private var imageFileName: String = "profileDefault"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        signupProgressBar.visibility = View.INVISIBLE

        signupAvatarImage.setOnClickListener { view -> generateUserAvatar(view) }
        signupAvatarImage.setOnLongClickListener { pickImage() }

    }

    fun pickImage() : Boolean{
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Intent>(pickIntent))

        startActivityForResult(chooserIntent, SELECT_PROFILE_IMAGE)

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PROFILE_IMAGE && resultCode == Activity.RESULT_OK){
            if(data != null){
                val imageUri = data.data;
                val cursor = contentResolver.query(imageUri!!, null, null,
                    null, null)
                val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor!!.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val imageSize = cursor.getLong(sizeIndex)
                imageFileName = cursor.getString(nameIndex)
                cursor.close()
                if(imageSize > MAX_IMAGE_FILE_SIZE){
                    Toast.makeText(this, "Select image less than 100KB",
                        Toast.LENGTH_SHORT).show()
                    return
                }
                signupAvatarImage.setImageURI(data.data)
            }
        }
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
        imageFileName = userAvatar

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
        var imageBitmap: Bitmap = (signupAvatarImage.drawable as BitmapDrawable).bitmap

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
                                AuthService.uploadPhoto(this, imageBitmap, imageFileName){ uploadSuccessful ->
                                    if(uploadSuccessful) {
                                        Toast.makeText(
                                            this, "User Created successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                        LocalBroadcastManager.getInstance(this)
                                            .sendBroadcast(userDataChange)
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

    private fun showProgressSpinner(show : Boolean){
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
