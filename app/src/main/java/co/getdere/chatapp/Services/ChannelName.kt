package co.getdere.chatapp.Services

import co.getdere.chatapp.Model.Channel

object ChannelName {
    private val listeners = ArrayList<(Channel?) -> Unit>()

    fun addChannelNameChangedListener(listener: (Channel?) -> Unit) {
        listeners.add(listener)

    }

    fun removeChannelNameChangedListener(listener: (Channel?) -> Unit) {
        listeners.remove(listener)
    }

    var activeChannel: Channel? = null
        set(value) {
            field = value
            listeners.forEach { it.invoke(value) }
        }
}