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
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import co.getdere.chatapp.Adapters.ChannelsAdapter
import co.getdere.chatapp.Model.Channel
import co.getdere.chatapp.R
import co.getdere.chatapp.Services.AuthService
import co.getdere.chatapp.Services.ChannelName
import co.getdere.chatapp.Services.UserDataService
import co.getdere.chatapp.Services.MessageService
import co.getdere.chatapp.Utilities.BROADCAST_USER_DATA_CHANGE
import co.getdere.chatapp.Utilities.ChannelClickListener
import co.getdere.chatapp.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)

    lateinit var channelsAdapter: ChannelsAdapter

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


        val channelsLayoutManager = LinearLayoutManager(this)
        channel_list.layoutManager = channelsLayoutManager
        channelsAdapter = ChannelsAdapter(this, MessageService.channels)
        channel_list.adapter = channelsAdapter
        //takes care of the recycler view of the channel list

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        //taps in to the socket to "listen" for creation of new channels


        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this, {})
        }
        // Checks if the user s logged in already in App.prefs (shared preferences) and if so logs them in again automatically using the findUserByEmail function




        channel_list.addOnItemTouchListener(RecyclerTouchListener(this,
            channel_list, object : ChannelClickListener {
                override fun onClick(view:View, position:Int) {
                    //Values are passing to activity & to fragment as well
                    Toast.makeText(parent, "Single Click on position :" + position,
                        Toast.LENGTH_SHORT).show()
                }

            }))






    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            userDataChangeReceiver, IntentFilter(
                BROADCAST_USER_DATA_CHANGE
            )
        )
        //Broadcasts there was a data change (or perhaps there wasn't but there is a need to let the app know it needs to check for the existing data?)

    }


    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect() // disconnects the socket on destroy, probably to save memory

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
                        if (MessageService.channels.count() > 0)//checks if there are existing channels
                        {
                            ChannelName.activeChannel = MessageService.channels[0]
                            channelsAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                })
            }
        }
    }

    fun updateWithChannel() {

        main_channel_name.text = "#${ChannelName.activeChannel?.name}" //change the "please login" to the name of the selected channel

    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun LoginBtnNavClicked(view: View) {

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
                .setPositiveButton("Add", { dialogInterface, i ->
                    val nameTextView = dialogView.findViewById<EditText>(R.id.add_channel_name_text)
                    val descriptionTextView = dialogView.findViewById<EditText>(R.id.add_channel_description_text)

                    val channelName = nameTextView.text.toString()
                    val channelDescription = descriptionTextView.text.toString()

                    socket.emit("newChannel", channelName, channelDescription)

                    channelsAdapter.notifyDataSetChanged()

                }
                ).setNegativeButton("Cancel", { dialogInterface, i ->

                    hideKeyboard()
                })
                .show()
        } else {
            Toast.makeText(this, "Please login to open channels", Toast.LENGTH_LONG).show()
        }

    }

    private val onNewChannel = Emitter.Listener { args ->
        runOnUiThread {

            val channelName = args[0] as String
            val channelDescription = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDescription, channelId)

            MessageService.channels.add(newChannel)
        }
    }

    fun sendMessageBtnClicked(view: View) {
        hideKeyboard()
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }


    internal class RecyclerTouchListener(context:Context, recycleView: RecyclerView, clickListener: ChannelClickListener):RecyclerView.OnItemTouchListener {
        private val clicklistener: ChannelClickListener
        private val gestureDetector: GestureDetector
        init{
            this.clicklistener = clickListener
            gestureDetector = GestureDetector(context, object:GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent):Boolean {
                    return true
                }
            })
        }
        override fun onInterceptTouchEvent(rv:RecyclerView, e:MotionEvent):Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e))
            {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child))
            }
            return false
        }
        override fun onTouchEvent(rv:RecyclerView, e:MotionEvent) {
        }
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept:Boolean) {
        }
    }




}
