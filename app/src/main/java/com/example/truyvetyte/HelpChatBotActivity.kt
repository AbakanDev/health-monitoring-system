package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HelpChatBotActivity : AppCompatActivity() {

    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // KẾT NỐI VỚI LAYOUT SẴN CÓ CỦA BẠN BẠN
        setContentView(R.layout.help_chatbot_screen)
        val rvChat = findViewById<RecyclerView>(R.id.rv_chat)
        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<ImageView>(R.id.btn_send) // Thay bằng ID nút gửi của bạn

        adapter = ChatAdapter(chatList)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = adapter

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                // 1. Thêm tin nhắn của User
                addMessage(userText, false)
                etMessage.text.clear()

                // 2. Bot trả lời sau 1 khoảng thời gian ngắn
                Handler(Looper.getMainLooper()).postDelayed({
                    botResponse(userText)
                }, 500)
            }
        }
    }
    private fun addMessage(text: String, isBot: Boolean) {
        chatList.add(ChatMessage(text, isBot))
        adapter.notifyItemInserted(chatList.size - 1)
        findViewById<RecyclerView>(R.id.rv_chat).scrollToPosition(chatList.size - 1)
    }

    private fun botResponse(userQuery: String) {
        val answer = when {
            userQuery.contains("chào", ignoreCase = true) -> "Chào bạn! Mình có thể giúp gì cho bạn?"
            userQuery.contains("khai báo", ignoreCase = true) -> "Bạn hãy nhấn vào nút Khai Báo ở thanh menu bên dưới nhé."
            userQuery.contains("covid", ignoreCase = true) -> "Hãy luôn đeo khẩu trang và rửa tay sát khuẩn bạn nhé!"
            else -> "Xin lỗi, mình chưa hiểu câu hỏi. Bạn có thể hỏi về 'khai báo' hoặc 'triệu chứng' không?"
        }
        addMessage(answer, true)
    }
}
