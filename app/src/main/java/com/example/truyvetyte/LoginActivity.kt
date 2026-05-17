package com.example.truyvetyte

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        // 1. Nút chuyển sang màn hình Đăng ký
        val btnGo = findViewById<Button>(R.id.btn_register)
        btnGo.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 2. Ánh xạ các view của màn hình Đăng nhập
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val etUsername = findViewById<EditText>(R.id.et_username) // Nhập Tên đăng nhập
        val etPassword = findViewById<EditText>(R.id.et_password) // Nhập Mật khẩu

        btnLogin.setOnClickListener {
            // Lấy dữ liệu và đồng bộ tên biến với backend
            val tenDangNhap = etUsername.text.toString().trim()
            val matKhau = etPassword.text.toString().trim()

            // Kiểm tra rỗng
            if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi API Login qua Retrofit
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Truyền chuẩn xác vào Model LoginRequest
                    val request = LoginRequest(TenDangNhap = tenDangNhap, MatKhau = matKhau)

                    val response = RetrofitClient.instance.loginUser(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            // Thành công (HTTP 200)
                            val body = response.body()
                            Toast.makeText(this@LoginActivity, body?.message, Toast.LENGTH_SHORT).show()

                            // Lấy thông tin từ backend trả về (Khớp với cục JSON)
                            val token = body?.token
                            val hoTen = body?.data?.HoTen ?: "Unknown"
                            val maNguoiDung = body?.data?.MaNguoiDung ?: -1  // Kiểu Int
                            val maVaiTro = body?.data?.MaVaiTro ?: ""        // Kiểu String (VD: "VT01")

                            // Lưu thông tin phiên đăng nhập vào SharedPreferences
                            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()

                            // Đặt Key lưu trữ đồng bộ 100% với tên Database
                            editor.putString("TOKEN", token)
                            editor.putInt("MaNguoiDung", maNguoiDung)
                            editor.putString("HoTen", hoTen)
                            editor.putString("MaVaiTro", maVaiTro)
                            editor.apply()

                            // Chuyển trang và truyền tên qua MainActivity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("HoTen", hoTen) // Đồng bộ luôn cả biến truyền qua Activity
                            startActivity(intent)

                            // Đóng hoàn toàn LoginActivity
                            finish()

                        } else {
                            // Xử lý lỗi (Tài khoản không tồn tại, sai pass... - HTTP 400, 401)
                            val errorString = response.errorBody()?.string()
                            if (errorString != null) {
                                try {
                                    val jsonObject = JSONObject(errorString)
                                    val message = jsonObject.getString("message")
                                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(this@LoginActivity, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@LoginActivity, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Lỗi rớt mạng hoặc sập server
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Lỗi server nội bộ: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}