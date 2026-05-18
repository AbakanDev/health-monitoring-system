package com.example.truyvetyte

import android.os.Bundle
import android.widget.TextView 
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testBackendConnection()
    }

    private fun testBackendConnection() {
        // Đẩy việc gọi mạng ra luồng IO ngầm
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Gọi tới backend Render
                val response = RetrofitClient.instance.checkHealth()

                // Chuyển về luồng Main để hiển thị thông báo lên màn hình
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val message = response.body()?.message
                        Toast.makeText(this@MainActivity, "✅ Thành công: $message", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "❌ Gọi API được nhưng bị lỗi: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Nếu app không thể tìm thấy server (thiếu mạng, sai link)
                    Toast.makeText(this@MainActivity, "💥 Lỗi sập nguồn: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}