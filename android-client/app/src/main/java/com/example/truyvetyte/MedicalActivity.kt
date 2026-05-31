package com.example.truyvetyte

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MedicalActivity : AppCompatActivity() {

    // Khai báo các TextView hiển thị số liệu
    private lateinit var tvTongCaBenh: TextView
    private lateinit var tvTongBenhNhan: TextView
    private lateinit var tvTiemChung: TextView
    private lateinit var tvXetNghiem: TextView
    private lateinit var tvCachLy: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.medical_activity_dashboard)

        // 1. Xử lý Header (Lời chào & Đăng xuất)
        val hoTen = intent.getStringExtra("HoTen") ?: "Cán bộ y tế"
        val tvTitleHeader = findViewById<TextView>(R.id.tvTitleHeader)
        tvTitleHeader.text = "Xin chào, $hoTen"

        val btnLogout = findViewById<ImageButton>(R.id.iv_logout_header)
        btnLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 2. Ánh xạ các view của Dashboard
        tvTongCaBenh = findViewById(R.id.tvTongCaBenh)
        tvTongBenhNhan = findViewById(R.id.tvTongBenhNhan)
        tvTiemChung = findViewById(R.id.tvTiemChung)
        tvXetNghiem = findViewById(R.id.tvXetNghiem)
        tvCachLy = findViewById(R.id.tvCachLy)

        // 3. Gọi hàm lấy dữ liệu từ Backend
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        lifecycleScope.launch {
            try {
                // Lấy Token từ SharedPreferences
                val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    Toast.makeText(this@MedicalActivity, "Lỗi: Không tìm thấy Token!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val formattedToken = if (token.startsWith("Bearer")) token else "Bearer $token"

                // Bắt đầu gọi API
                val response = RetrofitClient.instance.getDashboardOverview(formattedToken)

                if (response.isSuccessful && response.body() != null) {
                    val overviewResponse = response.body()!!

                    if (overviewResponse.success && overviewResponse.data != null) {
                        val data = overviewResponse.data

                        // Format số liệu thành dạng có dấu chấm
                        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

                        tvTongCaBenh.text = formatter.format(data.TongCaBenh)
                        tvTongBenhNhan.text = formatter.format(data.TongBenhNhan)
                        tvTiemChung.text = formatter.format(data.TiemChungThangNay)
                        tvXetNghiem.text = formatter.format(data.XetNghiemThangNay)
                        tvCachLy.text = formatter.format(data.CachLyThangNay)
                    } else {
                        Log.e("MedicalActivity", overviewResponse.message)
                    }
                } else {
                    Log.e("MedicalActivity", "Lỗi lấy dữ liệu từ server: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MedicalActivity", "Lỗi kết nối dashboard overview: ${e.message}")
            }
        }
    }
}