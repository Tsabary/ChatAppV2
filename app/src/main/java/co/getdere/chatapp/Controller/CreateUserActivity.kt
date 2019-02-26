package co.getdere.chatapp.Controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import co.getdere.chatapp.R
import co.getdere.chatapp.Services.AuthService
import co.getdere.chatapp.Services.UserDataService
import co.getdere.chatapp.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvater = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        create_spinner.visibility = View.INVISIBLE

    }

    fun generateUserAvatar(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color == 0) {
            userAvater = "light$avatar"
        } else {
            userAvater = "dark$avatar"
        }
        val resourceId = resources.getIdentifier(userAvater, "drawable", packageName)
        create_avatar_image_view.setImageResource(resourceId)
    }


    fun generateColorClicked(view: View) {

        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        create_avatar_image_view.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1"
    }


    fun createUserClicked(view: View) {
        val userName = create_user_name_text.text.toString()
        val email = create_email_text.text.toString()
        val password = create_password_text.text.toString()

        if (userName.length > 3) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                enableSpinner(true)
                AuthService.registerUser(email, password, { registerSuccess ->
                    if (registerSuccess) {
                        AuthService.loginUser(email, password, { loginSuccess ->
                            if (loginSuccess) {
                                AuthService.createUser(userName,email,userAvater, avatarColor, { createSuccess ->
                                    if (createSuccess) {
                                        enableSpinner(false)
                                        finish()
                                        val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                        LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                    } else {
                                        errorToast()
                                    }
                                })
                            } else {
                                errorToast()
                            }
                        })
                    } else {
                        errorToast()
                    }
                })
            } else {
                Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Name is too short.", Toast.LENGTH_LONG).show()
        }
    }


    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {

        if (enable) {
            create_spinner.visibility = View.VISIBLE

        } else {
            create_spinner.visibility = View.INVISIBLE
        }
        create_create_user_btn.isEnabled = !enable
        create_avatar_image_view.isEnabled = !enable
        create_background_color_btn.isEnabled = !enable

    }
}