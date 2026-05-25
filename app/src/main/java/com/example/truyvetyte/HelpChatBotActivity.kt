package com.example.truyvetyte

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.truyvetyte.model.AiAskRequest
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.launch

class HelpChatBotActivity : AppCompatActivity() {

    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_chatbot_screen)

        rvChat = findViewById(R.id.rv_chat)
        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<ImageView>(R.id.btn_send)

        adapter = ChatAdapter(chatList)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = adapter

        // Tin nhắn chào mừng
        addMessage("Xin chào! Mình là trợ lý sức khỏe AI. Bạn cần hỏi gì về dịch tễ, tiêm chủng hay khai báo y tế không?", isBot = true)

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                addMessage(userText, isBot = false)
                etMessage.text.clear()
                // Hiện "đang trả lời..." trong khi chờ
                addMessage("⏳ Đang xử lý...", isBot = true)
                askAI(userText)
            }
        }
        val btnBack = findViewById<ImageButton>(R.id.btnbacktohelp) // Thay bằng ID nút của bạn

        btnBack.setOnClickListener {
            // Cách 1: Bắt chước hệt như bấm nút Back hệ thống (có xử lý cả quay lại Fragment)
            onBackPressedDispatcher.onBackPressed()

            // Cách 2: Nếu chỉ muốn đóng Activity hiện tại để về Activity trước đó
            // finish()
        }
    }

    private fun askAI(question: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.askHealthAI(
                    body = AiAskRequest(question = question)
                )

                // Xoá tin nhắn "đang xử lý..." vừa thêm
                removeLastBotMessage()

                if (response.isSuccessful) {
                    val answer = response.body()?.answer
                    if (!answer.isNullOrBlank()) {
                        addMessage(answer, isBot = true)
                    } else {
                        addMessage("Xin lỗi, mình không nhận được phản hồi. Bạn thử lại nhé!", isBot = true)
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        503 -> "AI đang quá tải, bạn vui lòng thử lại sau ít phút nhé."
                        400 -> "Câu hỏi không hợp lệ, bạn thử diễn đạt lại xem sao."
                        else -> "Có lỗi xảy ra (${response.code()}), bạn thử lại sau nhé."
                    }
                    addMessage(errorMsg, isBot = true)
                }

            } catch (e: Exception) {
                removeLastBotMessage()
                addMessage("Không kết nối được server. Kiểm tra mạng và thử lại nhé!", isBot = true)
            }
        }
    }

    private fun addMessage(text: String, isBot: Boolean) {
        chatList.add(ChatMessage(text, isBot))
        adapter.notifyItemInserted(chatList.size - 1)
        rvChat.scrollToPosition(chatList.size - 1)
    }

    private fun removeLastBotMessage() {
        val lastIndex = chatList.indexOfLast { it.isBot }
        if (lastIndex != -1) {
            chatList.removeAt(lastIndex)
            adapter.notifyItemRemoved(lastIndex)
        }
    }
}