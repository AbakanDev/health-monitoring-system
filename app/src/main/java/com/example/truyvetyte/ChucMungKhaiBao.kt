package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ChucMungKhaiBao : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // KẾT NỐI VỚI LAYOUT SẴN CÓ CỦA BẠN BẠN
        setContentView(R.layout.congratulation_declaration)
        val btnBack = findViewById<ImageButton>(R.id.btnNext)
        // Khi bấm nút quay về ở Trang 2
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            // 1. Gửi mật mã để MainActivity biết phải mở mục nào
            intent.putExtra("ACTION", "GO_TO_XU_HUONG")

            // 2. Dọn dẹp bộ nhớ để quay về bản MainActivity duy nhất
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
            finish() // Đóng trang 2 lại luôn
        }
    }
}

