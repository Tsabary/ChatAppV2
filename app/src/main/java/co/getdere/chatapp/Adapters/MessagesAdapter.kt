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
import co.getdere.chatapp.Services.UserDataService

class MessagesAdapter(val context: Context, val messages: ArrayList<Message>) :
    RecyclerView.Adapter<MessagesAdapter.Holder>() {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val msgWriter = itemView.findViewById<TextView>(R.id.msg_writer)
        val msgDate = itemView.findViewById<TextView>(R.id.msg_date)
        val msgBody = itemView.findViewById<TextView>(R.id.msg_body)
        val msgAvatar = itemView.findViewById<ImageView>(R.id.msg_avatar)

        fun bindMessage(message: Message, context: Context) {
            msgWriter.text = message.userName
            msgDate.text = message.timeStamp
            msgBody.text = message.messageBody
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)
            msgAvatar.setImageResource(resourceId)
            msgAvatar.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesAdapter.Holder {

        val view = LayoutInflater.from(context).inflate(R.layout.msg_layout, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bindMessage(messages[position], context)

    }
}