package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
// BỔ SUNG THÊM 3 IMPORT NÀY
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sign_in_screen)

        val btnBack = findViewById<ImageButton>(/* id = */ R.id.btnBack)
        btnBack.setOnClickListener {
            // Chuyển từ trang hiện tại (this) sang FriendActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
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

            // 1. Kiểm tra dữ liệu trống
            if (cccd.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ CCCD và Mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Kiểm tra mật khẩu khớp nhau
            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Dùng Intent ném CCCD và Mật khẩu sang màn hình PersonalDetailActivity
            val nextIntent = Intent(this, PersonalDetailActivity::class.java)
            nextIntent.putExtra("EXTRA_CCCD", cccd)
            nextIntent.putExtra("EXTRA_PASSWORD", password)
            startActivity(nextIntent)

            // Bạn có thể thêm animation chuyển trang sang phải ở đây nếu muốn đồng bộ với nút Back
            // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}