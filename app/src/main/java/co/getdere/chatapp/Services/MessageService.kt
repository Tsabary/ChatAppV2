package co.getdere.chatapp.Services

import android.content.Context
import android.util.Log
import co.getdere.chatapp.Controller.App
import co.getdere.chatapp.Model.Channel
import co.getdere.chatapp.Utilities.URL_GET_CHANNELS
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()

    fun getChannels(complete: (Boolean) -> Unit) {

        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->

                try {

                    for (x in 0 until (response.length())) {
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

}