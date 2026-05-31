package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nhớ đảm bảo tên layout đúng với file XML số 1 của bạn
        setContentView(R.layout.sign_in_screen)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        val edtCccd = findViewById<AppCompatEditText>(R.id.edtCccd)
        val edtPassword = findViewById<AppCompatEditText>(R.id.edtPassword)
        val edtConfirmPassword = findViewById<AppCompatEditText>(R.id.edtConfirmPassword)
        val btnRegisterSubmit = findViewById<AppCompatButton>(R.id.btnRegisterSubmit)

        btnRegisterSubmit.setOnClickListener {
            val cccd = edtCccd.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            // 1. Kiểm tra rỗng
            if (cccd.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ CCCD và Mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Kiểm tra mật khẩu khớp nhau
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Đóng gói CCCD và Password, chuyển sang trang Điền thông tin
            val nextIntent = Intent(this, PersonalDetailActivity::class.java)
            nextIntent.putExtra("EXTRA_CCCD", cccd)
            nextIntent.putExtra("EXTRA_PASSWORD", password)
            startActivity(nextIntent)

            // Tùy chọn: Thêm hiệu ứng chuyển trang
            // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}