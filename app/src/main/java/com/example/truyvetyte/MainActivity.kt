package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Kiểm tra xem có yêu cầu mở Fragment cụ thể không
        val target = intent.getStringExtra("open_fragment")
        if (target == "KHAI_BAO") {
            replaceFragment(KhaiBao()) // Hàm replaceFragment bạn đã viết trước đó

            // (Tùy chọn) Cập nhật lại màu sắc icon trên thanh Bottom Nav cho đúng
            // bottomNavigation.selectedItemId = R.id.nav_khai_bao
        } else {
            // Mặc định mở trang chủ nếu không có yêu cầu gì
            replaceFragment(XuHuong())
        }

        testBackendConnection()

        val btnXuHuong = findViewById<ImageButton>(R.id.btn_dashboard)
        val btnLichSu = findViewById<ImageButton>(R.id.btn_history)
        val btnDichTe = findViewById<ImageButton>(R.id.btn_epidemiology)
        val btnKhaiBao = findViewById<ImageButton>(R.id.btn_health_form)
        val btnKhaiBao2 = findViewById<ImageButton>(R.id.btn_xnc)
        val btnQuetQR = findViewById<ImageButton>(R.id.btn_qr_scan)
        val btnHelp = findViewById<ImageButton>(R.id.btn_help)
        // Trong onCreate, bạn gọi hàm này khi bấm vào các nút ở Bottom Nav
        btnXuHuong.setOnClickListener {
            replaceFragment(XuHuong())
        }
        btnLichSu.setOnClickListener {
            replaceFragment(LichSuTruyVet())
        }
        btnDichTe.setOnClickListener {
            replaceFragment(DichTe())
        }
        btnKhaiBao.setOnClickListener {
            replaceFragment(KhaiBao())
        }
        btnKhaiBao2.setOnClickListener {
            replaceFragment(KhaiBao2())
        }
        // Trong MainActivity.kt, tại chỗ bắt sự kiện nút QR
        btnQuetQR.setOnClickListener {
            replaceFragment(ScannerFragment())
        }
        btnHelp.setOnClickListener {
            replaceFragment(HelpActivity())
        }
        // Xử lý khi app vừa mở lên
        handleIntent(intent)
    }

    // Cực kỳ quan trọng: Hàm này chạy khi quay về từ Trang 2 bằng FLAG_ACTIVITY_SINGLE_TOP
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Cập nhật dữ liệu mới nhất
        handleIntent(intent)
    }
    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra("ACTION")

        if (action == "GO_TO_XU_HUONG") {
            replaceFragment(XuHuong())

            // Thay vì dùng bottomNav.selectedItemId, hãy tự đổi màu nút nếu muốn:
            // val btnXuHuong = findViewById<ImageButton>(R.id.btn_dashboard)
            // btnXuHuong.setImageResource(R.drawable.ic_dashboard_active)
        }
    }
    // Hàm dùng chung để thay đổi Fragment
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // R.id.main_content_frame là ID của FrameLayout ở Bước 2
        fragmentTransaction.replace(R.id.main_content_frame, fragment)

        fragmentTransaction.commit()
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