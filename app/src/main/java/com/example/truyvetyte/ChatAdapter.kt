package com.example.truyvetyte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val chatList: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_USER = 1
    private val TYPE_BOT = 2

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].isBot) TYPE_BOT else TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatList[position]
        if (holder is UserViewHolder) {
            holder.txtMessage.text = message.text
        } else if (holder is BotViewHolder) {
            holder.txtMessage.text = message.text
        }
    }

    override fun getItemCount() = chatList.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.tv_user_message) // Thay bằng ID TextView trong layout user của bạn
    }

    class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.tv_bot_message) // Thay bằng ID TextView trong layout bot của bạn
    }
}