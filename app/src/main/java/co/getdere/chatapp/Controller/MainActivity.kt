package co.getdere.chatapp.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import co.getdere.chatapp.Adapters.ChannelsAdapter
import co.getdere.chatapp.Adapters.MessagesAdapter
import co.getdere.chatapp.Model.Channel
import co.getdere.chatapp.Model.Message
import co.getdere.chatapp.R
import co.getdere.chatapp.Services.AuthService
import co.getdere.chatapp.Services.ChannelName
import co.getdere.chatapp.Services.UserDataService
import co.getdere.chatapp.Services.MessageService
import co.getdere.chatapp.Utilities.BROADCAST_USER_DATA_CHANGE
import co.getdere.chatapp.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelTitle: TextView


    lateinit var channelsAdapter: ChannelsAdapter
    lateinit var channelsLayoutManager: LinearLayoutManager

    lateinit var messagesAdapter: MessagesAdapter
    lateinit var messageLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()


        messageLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        messagesAdapter = MessagesAdapter(this, MessageService.messages)
        //takes care of the recycler view of the messages in the chat

        channelsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        channelsAdapter = ChannelsAdapter(this, MessageService.channels)

        channel_list.layoutManager = channelsLayoutManager
        channel_list.adapter = channelsAdapter
        //takes care of the recycler view of the channel list

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        //taps in to the socket to "listen" for creation of new channels
        socket.on("messageCreated", onNewMessage)
        //taps in to the socket to "listen" for creation of new messages


        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this, {})
        }
        // Checks if the user s logged in already in App.prefs (shared preferences) and if so logs them in again automatically using the findUserByEmail function

        channelTitle = findViewById(R.id.main_channel_name)

        ChannelName.addChannelNameChangedListener {
            // Do your operation
            drawer_layout.closeDrawer(GravityCompat.START)
            channelTitle.text = "#${ChannelName.activeChannel?.name}"
            updateWithChannel()

        }


    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            userDataChangeReceiver, IntentFilter(
                BROADCAST_USER_DATA_CHANGE
            )
        )
        /*Broadcasts there was a data change
        (or perhaps there wasn't but there is a need to let the app know it needs to check for the existing data?)
         */

    }


    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        // disconnects the socket on destroy, probably to save memory

        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        // disconnects the broadcaster on destroy, probably to save memory
    }


    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                user_name_nav_header.text = UserDataService.name
                user_email_nav_header.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                user_image_nav_header.setImageResource(resourceId)
                user_image_nav_header.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))

                login_button_nav_header.text = getString(R.string.Log_out)

                MessageService.getChannels({ complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0)
                        //checks if there are existing channels
                        {
                            ChannelName.activeChannel = MessageService.channels[0]
                            channelsAdapter.notifyDataSetChanged()
                        }
                    }
                })
            }
        }
    }

    fun updateWithChannel() {

        main_channel_name.text = "#${ChannelName.activeChannel?.name}"
        //change the "please login" to the name of the selected channel

        if (ChannelName.activeChannel != null) {
            MessageService.getMessages(ChannelName.activeChannel!!.id, { complete ->
                if (complete) {
                    for (message in MessageService.messages) {
                        println(message.messageBody)
                    }
                }
            })
        }


    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNavClicked(view: View) {

        if (App.prefs.isLoggedIn) {
            UserDataService.logOut()
            login_button_nav_header.text = getString(R.string.Login)
            user_email_nav_header.text = ""
            user_name_nav_header.text = ""
            user_image_nav_header.setImageResource(R.drawable.profiledefault)
            user_image_nav_header.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }


    }


    fun addChannelClicked(view: View) {

        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Add", { _, _ ->
                    val nameTextView = dialogView.findViewById<EditText>(R.id.add_channel_name_text)
                    val descriptionTextView = dialogView.findViewById<EditText>(R.id.add_channel_description_text)

                    val channelName = nameTextView.text.toString()
                    val channelDescription = descriptionTextView.text.toString()

                    socket.emit("newChannel", channelName, channelDescription)

                    channelsAdapter.notifyDataSetChanged()

                }
                ).setNegativeButton("Cancel", { _, _ ->

                    hideKeyboard()
                })
                .show()
        } else {
            Toast.makeText(this, "Please login to open channels", Toast.LENGTH_LONG).show()
        }

    }

    private val onNewChannel = Emitter.Listener { args ->

        if (App.prefs.isLoggedIn) {
            runOnUiThread {

                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)

                MessageService.channels.add(newChannel)
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {

                val channelId = args[2] as String
                if (channelId == ChannelName.activeChannel?.id) {

                    val message = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(message, channelId, userName, userAvatar, userAvatarColor, id, timeStamp)
                    MessageService.messages.add(newMessage)
                }

            }


        }
    } /*This function adds new messages to the array of messages.
    It is used when the socket listener receives a signal/information that there is a new messageBody.
    It then checks if the messageBody's channel id (the channel it was sent to) is the same at the current channel the user is on.
    if it is then it adds it to the array of messages.
    THe reason it makes the channel check first is because our array only holds the messages of the current selected channel*/

    fun sendMessageBtnClicked(view: View) {

        if (App.prefs.isLoggedIn && channelTitle.text.isNotEmpty()) {

            val userId = UserDataService.id
            val channelId = ChannelName.activeChannel!!.id

            socket.emit(
                "newMessage",
                msg_text_field.text.toString(),
                userId,
                channelId,
                UserDataService.name,
                UserDataService.avatarName,
                UserDataService.avatarColor
            )

            msg_text_field.text.clear()
            hideKeyboard()

        }


    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

}
