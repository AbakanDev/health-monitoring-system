package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RegisterSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.congratulation_login)

        val btnNext = findViewById<ImageView>(R.id.btnNext)

        btnNext.setOnClickListener {
            // Chuyển hướng về LoginActivity
            val intent = Intent(this, LoginActivity::class.java)

            // Xóa toàn bộ lịch sử màn hình trước đó
            // Để khi user về màn hình Login mà bấm nút Back trên điện thoại thì app sẽ thoát,
            // chứ không quay ngược lại màn hình đăng ký thành công nữa.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}