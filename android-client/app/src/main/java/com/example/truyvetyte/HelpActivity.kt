package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

class HelpActivity : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Kết nối với layout help_screen
        return inflater.inflate(R.layout.help_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ==========================================
        // 1. XỬ LÝ CHUYỂN TRANG CHATBOT
        // ==========================================
        val btnGo = view.findViewById<ImageButton>(R.id.btn_girl_chatbot)
        btnGo.setOnClickListener {
            // Sử dụng Intent để mở Activity mới
            val intent = Intent(requireContext(), HelpChatBotActivity::class.java)
            startActivity(intent)
        }

        // ==========================================
        // 2. XỬ LÝ ẨN/HIỆN CÂU HỎI THƯỜNG GẶP (FAQ)
        // ==========================================
        // Gọi hàm hỗ trợ cho cả 3 câu hỏi, truyền thêm biến 'view' vào
        setupFaqToggle(view, R.id.btn_expand_faq_1, R.id.tv_content_faq_1)
        setupFaqToggle(view, R.id.btn_expand_faq_2, R.id.tv_content_faq_2)
        setupFaqToggle(view, R.id.btn_expand_faq_3, R.id.tv_content_faq_3)
    }

    /**
     * Hàm hỗ trợ xử lý sự kiện click cho các nút mũi tên.
     * Vì ở trong Fragment, ta cần truyền View vào để tìm kiếm các ID.
     */
    private fun setupFaqToggle(view: View, buttonId: Int, contentId: Int) {
        val button = view.findViewById<ImageButton>(buttonId)
        val content = view.findViewById<TextView>(contentId)

        button.setOnClickListener {
            if (content.visibility == View.GONE) {
                // Đang ẩn -> Hiện nội dung và đổi thành mũi tên lên
                content.visibility = View.VISIBLE
                button.setImageResource(android.R.drawable.arrow_up_float)
            } else {
                // Đang hiện -> Ẩn nội dung và trả về mũi tên xuống
                content.visibility = View.GONE
                button.setImageResource(android.R.drawable.arrow_down_float)
            }
        }
    }
}