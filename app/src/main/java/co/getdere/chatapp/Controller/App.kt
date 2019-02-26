package co.getdere.chatapp.Controller

import android.app.Application
import co.getdere.chatapp.Utilities.SharedPrefs

class App : Application() {

    companion object {
        lateinit var prefs : SharedPrefs
    }

    override fun onCreate() {

        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}