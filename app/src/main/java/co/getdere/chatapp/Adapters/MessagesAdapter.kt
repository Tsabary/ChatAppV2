package co.getdere.chatapp.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import co.getdere.chatapp.Model.Message

class MessagesAdapter(val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<> {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) {

    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    fun onBindViewHolder(holder: MessagesAdapter.Holder, position: Int){

    }
}