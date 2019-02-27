package co.getdere.chatapp.Services

import android.content.Context
import android.util.Log
import co.getdere.chatapp.Controller.App
import co.getdere.chatapp.Model.Channel
import co.getdere.chatapp.Model.Message
import co.getdere.chatapp.Utilities.URL_GET_CHANNELS
import co.getdere.chatapp.Utilities.URL_GET_MESSAGES
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {

        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->

                try {

                    for (x in 0 until response.length()) {
                        val channel = response.getJSONObject(x)
                        val channelName = channel.getString("name")
                        val channelDescription = channel.getString("description")
                        val channelId = channel.getString("_id")

                        val newChannel = Channel(channelName, channelDescription, channelId)
                        this.channels.add(newChannel)

                    }
                    complete(true)

                } catch (e: JSONException) {
                    Log.d("JSON", "EXC:" + e.localizedMessage)
                    complete(false)
                }

            }, Response.ErrorListener {
                Log.d("ERROR", "Could not retrieve channels")
                complete(false)
            }) {

                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                    return headers
                }
            }

        App.prefs.requestQueue.add(channelsRequest)

    }

    fun getMessages (channelId : String, complete: (Boolean) -> Unit){

        val url = "$URL_GET_MESSAGES$channelId"

        val messageRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
            clearMessages()

            try {

                for (x in 0 until response.length()) {
                    val message = response.getJSONObject(x)
                    val messageId = message.getString("_id")
                    val messageBody = message.getString("messageBody")
                    val messageChannelId = message.getString("channelId")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")


                    val newMessage = Message(messageBody, messageChannelId, userName, userAvatar, userAvatarColor, messageId, timeStamp)
                    this.messages.add(newMessage)
                }
                complete(true)

            } catch (e: JSONException) {
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