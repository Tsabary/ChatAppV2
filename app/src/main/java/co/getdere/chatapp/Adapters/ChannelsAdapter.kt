package co.getdere.chatapp.Adapters

import android.content.Context
import android.support.v4.view.GravityCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.getdere.chatapp.Model.Channel
import co.getdere.chatapp.R
import co.getdere.chatapp.Services.ChannelName
import kotlinx.android.synthetic.main.activity_main.view.*

class ChannelsAdapter(val context: Context, val channels: ArrayList<Channel>) :
    RecyclerView.Adapter<ChannelsAdapter.Holder>() {


    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val singleChannel = itemView.findViewById<TextView>(R.id.single_channel)

        val mainLayout = LayoutInflater.from(context).inflate(R.layout.activity_main, null)

        fun bindText(textVar: String, context: Context) {
            singleChannel.text = textVar
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindText(channels[position].toString(), context)

        holder.itemView.setOnClickListener {

            ChannelName.activeChannel = channels[position]

            holder.mainLayout.drawer_layout.closeDrawer(GravityCompat.START)

        }

    }

    override fun getItemCount(): Int {
        return channels.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsAdapter.Holder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel_list_layout, parent, false)
        return Holder(view)

    }

}


