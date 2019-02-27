package co.getdere.chatapp.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.getdere.chatapp.Model.Message
import co.getdere.chatapp.R
import co.getdere.chatapp.Services.ChannelName
import co.getdere.chatapp.Services.MessageService.channels
import co.getdere.chatapp.Services.UserDataService

class MessagesAdapter(val context: Context, val messages: ArrayList<Message>) :
    RecyclerView.Adapter<MessagesAdapter.Holder>() {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val msgWriter = itemView.findViewById<TextView>(R.id.msg_writer)
        val msgDate = itemView.findViewById<TextView>(R.id.msg_date)
        val msgBody = itemView.findViewById<TextView>(R.id.msg_body)
        val msgAvatar = itemView.findViewById<ImageView>(R.id.msg_avatar)

        fun bindElements(
            writer: String,
            date: String,
            body: String,
            avatarImage: String,
            avatarColor: String,
            context: Context
        ) {
            msgWriter.text = writer
            msgDate.text = date
            msgBody.text = body
            val resourceId = context.resources.getIdentifier(avatarImage, "drawable", context.packageName)
            msgAvatar.setImageResource(resourceId)
            val colorToInt = UserDataService.returnAvatarColor(avatarColor)
            msgAvatar.setBackgroundColor(colorToInt)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesAdapter.Holder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bindElements(
            messages[position].userName,
            messages[position].timeStamp,
            messages[position].messageBody,
            messages[position].userAvatar,
            messages[position].userAvatarColor,
            context
        )

    }
}