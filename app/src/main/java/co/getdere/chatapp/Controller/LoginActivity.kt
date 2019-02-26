package co.getdere.chatapp.Controller

import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import co.getdere.chatapp.R
import co.getdere.chatapp.Services.AuthService
import co.getdere.chatapp.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_spinner.visibility = View.INVISIBLE
    }

    fun loginLoginBtnClicked(view: View) {

        val email = login_email_text.text.toString()
        val password = login_password_text.text.toString()
        hideKeyboard()

        enableSpinner(true)
        if (email.isNotEmpty() && password.isNotEmpty()) {

            AuthService.loginUser(email, password, { loginSuccess ->

                if (loginSuccess) {
                    AuthService.findUserByEmail(this, { findSuccess ->
                        if (findSuccess) {
                            enableSpinner(false)
                            finish()

                        } else {
                            errorToast()
                        }
                    })
                } else {
                    errorToast()
                }
            })
        } else {
            Toast.makeText(this, "Plese fill in your email and password", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }


    }

    fun loginCreateUserBtnClicked(view: View) {
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {

        if (enable) {
            login_spinner.visibility = View.VISIBLE

        } else {
            login_spinner.visibility = View.INVISIBLE
        }
        login_email_text.isEnabled = !enable
        login_password_text.isEnabled = !enable
        login_btn.isEnabled = !enable
        login_create_user_btn.isEnabled = !enable

    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}

