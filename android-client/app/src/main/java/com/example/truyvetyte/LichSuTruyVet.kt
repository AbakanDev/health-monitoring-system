package com.example.truyvetyte

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LichSuTruyVet : Fragment() {

    // Views cho Tổng Quan
    private lateinit var tvF0Count: TextView
    private lateinit var tvF1Count: TextView
    private lateinit var tvF2Count: TextView
    private lateinit var tvTotalContactCount: TextView

    // Container cho Lịch Sử Tiếp Xúc động
    private lateinit var llContactContainer: LinearLayout

    // Views cho Tổng Quan Khu Vực
    private lateinit var tvDangerAreaCount: TextView
    private lateinit var tvRiskAreaCount: TextView
    private lateinit var tvSafeAreaCount: TextView
    private lateinit var tvTotalCheckInCount: TextView

    // Container cho Lịch Sử Check-In động
    private lateinit var llCheckInContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.contact_tracing_screen, container, false)

        // 1. Ánh xạ View Tổng Quan
        tvF0Count = view.findViewById(R.id.tvF0Count)
        tvF1Count = view.findViewById(R.id.tvF1Count)
        tvF2Count = view.findViewById(R.id.tvF2Count)
        tvTotalContactCount = view.findViewById(R.id.tvTotalContactCount)

        // 2. Ánh xạ View Container Lịch Sử Tiếp Xúc
        llContactContainer = view.findViewById(R.id.llContactContainer)

        // 3. Ánh xạ View Tổng Quan Khu Vực (Check-in)
        tvDangerAreaCount = view.findViewById(R.id.tvDangerAreaCount)
        tvRiskAreaCount = view.findViewById(R.id.tvRiskAreaCount)
        tvSafeAreaCount = view.findViewById(R.id.tvSafeAreaCount)
        tvTotalCheckInCount = view.findViewById(R.id.tvTotalCheckInCount)

        // 4. Ánh xạ View Container Lịch Sử Check-In
        llCheckInContainer = view.findViewById(R.id.llCheckInContainer)

        // 5. Gọi API
        fetchContactStats()
        fetchContactHistory()
        fetchCheckinStats()
        fetchCheckinHistory()

        return view
    }

    // --- HÀM LẤY THỐNG KÊ CHECK-IN --- //
    private fun fetchCheckinStats() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) return
        val bearerToken = "Bearer $token"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getCheckinStats(bearerToken, cccd)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let { stats ->
                            tvDangerAreaCount.text = stats.SoLuotNguyHiem
                            tvRiskAreaCount.text = stats.SoLuotNguyCo
                            tvSafeAreaCount.text = stats.SoLuotAnToan
                            tvTotalCheckInCount.text = stats.TongLuotCheckIn.toString()
                        }
                    } else {
                        tvDangerAreaCount.text = "0"
                        tvRiskAreaCount.text = "0"
                        tvSafeAreaCount.text = "0"
                        tvTotalCheckInCount.text = "0"
                    }
                }
            } catch (e: Exception) {
                Log.e("LichSuTruyVet", "Lỗi kết nối thống kê check-in: ${e.message}")
            }
        }
    }

    // --- HÀM LẤY LỊCH SỬ CHECK-IN --- //
    private fun fetchCheckinHistory() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) return
        val bearerToken = "Bearer $token"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getCheckinHistory(bearerToken, cccd)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val listData = response.body()?.data ?: emptyList()

                        // Xoá hết item cũ trong container rồi build lại từ đầu
                        llCheckInContainer.removeAllViews()

                        listData.forEach { item ->
                            val status = item.TrangThaiKhuVuc ?: "An Toàn"

                            // Màu sắc theo trạng thái
                            val (borderColor, badgeBg, badgeText, textColor) = when (status) {
                                "Nguy Hiểm" -> listOf("#E53935", "#EF9A9A", "#C62828", "#C62828")
                                "Nguy Cơ"   -> listOf("#FFF176", "#FFF59D", "#F57F17", "#F57F17")
                                else        -> listOf("#81C784", "#A5D6A7", "#2E7D32", "#2E7D32")
                            }

                            // --- Tạo cấu trúc giống hệt XML item gốc ---
                            // CardView ngoài
                            val outerCard = androidx.cardview.widget.CardView(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).also { it.setMargins(4, 4, 4, 4) }
                                radius = 8f * resources.displayMetrics.density
                                cardElevation = 3f * resources.displayMetrics.density
                            }

                            // Row ngoài (horizontal)
                            val rowOuter = LinearLayout(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                orientation = LinearLayout.HORIZONTAL
                            }

                            // Thanh màu bên trái
                            val leftBorder = View(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    (6 * resources.displayMetrics.density).toInt(),
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(Color.parseColor(borderColor))
                            }

                            // Row nội dung (horizontal, padding)
                            val rowInner = LinearLayout(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                orientation = LinearLayout.HORIZONTAL
                                gravity = android.view.Gravity.CENTER_VERTICAL
                                val p = (12 * resources.displayMetrics.density).toInt()
                                setPadding(p, p, p, p)
                            }

                            // Cột tên + giờ (bên trái, weight=1)
                            val colText = LinearLayout(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                )
                                orientation = LinearLayout.VERTICAL
                            }

                            val tvLocation = TextView(requireContext()).apply {
                                text = item.TenKhuVuc ?: "Không rõ"
                                setTextColor(Color.parseColor("#64B5F6"))
                                textSize = 16f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                            }

                            val tvTime = TextView(requireContext()).apply {
                                text = item.ThoiGianCheckIn ?: "Không rõ"
                                setTextColor(Color.parseColor("#90CAF9"))
                                textSize = 14f
                                (layoutParams as? LinearLayout.LayoutParams)?.topMargin = 2
                            }

                            colText.addView(tvLocation)
                            colText.addView(tvTime)

                            // Badge trạng thái (bên phải)
                            val badgeCard = androidx.cardview.widget.CardView(requireContext()).apply {
                                val w = (50 * resources.displayMetrics.density).toInt()
                                val h = (40 * resources.displayMetrics.density).toInt()
                                layoutParams = LinearLayout.LayoutParams(w, h)
                                radius = 6f * resources.displayMetrics.density
                                cardElevation = 0f
                                setCardBackgroundColor(Color.parseColor(badgeBg))
                            }

                            val tvStatus = TextView(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                                text = status.replace(" ", "\n")
                                setTextColor(Color.parseColor(textColor))
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                textSize = 11f
                                gravity = android.view.Gravity.CENTER
                            }

                            // Lắp ráp
                            badgeCard.addView(tvStatus)
                            rowInner.addView(colText)
                            rowInner.addView(badgeCard)

                            rowOuter.addView(leftBorder)
                            rowOuter.addView(rowInner)

                            outerCard.addView(rowOuter)
                            llCheckInContainer.addView(outerCard)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LichSuTruyVet", "Lỗi kết nối lịch sử check-in: ${e.message}")
            }
        }
    }

    private fun fetchContactStats() {
        // Đồng bộ cách gọi SharedPreferences giống hệt DichTe.kt
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy Token hoặc CCCD. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
            return
        }

        // Định dạng lại token để gửi lên Header
        val bearerToken = "Bearer $token"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Gọi API qua RetrofitClient
                val response = RetrofitClient.instance.getContactStats(bearerToken, cccd)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success) {
                            // Cập nhật UI nếu lấy data thành công
                            body.data?.let { stats ->
                                tvF0Count.text = stats.SoLuotF0
                                tvF1Count.text = stats.SoLuotF1
                                tvF2Count.text = stats.SoLuotF2
                                tvTotalContactCount.text = stats.TongLuotTiepXuc.toString()
                            }
                        } else {
                            // Nếu API trả về success = false
                            Toast.makeText(requireContext(), body?.message ?: "Chưa có dữ liệu tiếp xúc", Toast.LENGTH_SHORT).show()

                            // Gán mặc định bằng 0 nếu chưa tiếp xúc ai
                            tvF0Count.text = "0"
                            tvF1Count.text = "0"
                            tvF2Count.text = "0"
                            tvTotalContactCount.text = "0"
                        }
                    } else {
                        Toast.makeText(requireContext(), "Lỗi Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LichSuTruyVet", "Lỗi kết nối: ${e.message}")
                    Toast.makeText(requireContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchContactHistory() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) return

        val bearerToken = "Bearer $token"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getContactHistory(bearerToken, cccd)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success && !body.data.isNullOrEmpty()) {
                            val listData = body.data

                            // Xoá hết item cũ trong container rồi build lại từ đầu
                            llContactContainer.removeAllViews()

                            listData.forEach { item ->
                                val level = item.capDoDichTeHienTai ?: "F2"

                                // Màu sắc theo cấp độ dịch tễ
                                val (borderColor, badgeBg, badgeTextColor) = when (level) {
                                    "F0" -> Triple("#D32F2F", "#EF9A9A", "#B71C1C")
                                    "F1" -> Triple("#B55AE0", "#E8B8FA", "#B55AE0")
                                    else -> Triple("#42A5F5", "#BBDEFB", "#1565C0") // F2
                                }

                                // CardView ngoài
                                val outerCard = androidx.cardview.widget.CardView(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).also { it.setMargins(4, 4, 4, 4) }
                                    radius = 8f * resources.displayMetrics.density
                                    cardElevation = 3f * resources.displayMetrics.density
                                }

                                // Row ngoài (horizontal)
                                val rowOuter = LinearLayout(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    orientation = LinearLayout.HORIZONTAL
                                }

                                // Thanh màu bên trái
                                val leftBorder = View(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        (6 * resources.displayMetrics.density).toInt(),
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                    )
                                    setBackgroundColor(Color.parseColor(borderColor))
                                }

                                // Row nội dung (horizontal, padding)
                                val rowInner = LinearLayout(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    orientation = LinearLayout.HORIZONTAL
                                    gravity = android.view.Gravity.CENTER_VERTICAL
                                    val p = (12 * resources.displayMetrics.density).toInt()
                                    setPadding(p, p, p, p)
                                }

                                // Badge F0/F1/F2 (bên trái)
                                val badgeCard = androidx.cardview.widget.CardView(requireContext()).apply {
                                    val w = (50 * resources.displayMetrics.density).toInt()
                                    val h = (40 * resources.displayMetrics.density).toInt()
                                    layoutParams = LinearLayout.LayoutParams(w, h).also {
                                        it.marginEnd = (12 * resources.displayMetrics.density).toInt()
                                    }
                                    radius = 6f * resources.displayMetrics.density
                                    cardElevation = 0f
                                    setCardBackgroundColor(Color.parseColor(badgeBg))
                                }

                                val tvLevel = TextView(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                    )
                                    text = level
                                    setTextColor(Color.parseColor(badgeTextColor))
                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                    textSize = 18f
                                    gravity = android.view.Gravity.CENTER
                                }

                                // Cột tên + giờ (bên phải, weight=1, gravity=end)
                                val colText = LinearLayout(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                    )
                                    orientation = LinearLayout.VERTICAL
                                    gravity = android.view.Gravity.END
                                }

                                val tvLocation = TextView(requireContext()).apply {
                                    text = item.diaDiemTiepXuc ?: "Không rõ"
                                    setTextColor(Color.parseColor("#64B5F6"))
                                    textSize = 16f
                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                }

                                val tvTime = TextView(requireContext()).apply {
                                    text = item.thoiGianTiepXuc ?: "Không rõ"
                                    setTextColor(Color.parseColor("#90CAF9"))
                                    textSize = 14f
                                    (layoutParams as? LinearLayout.LayoutParams)?.topMargin = 2
                                }

                                // Lắp ráp
                                badgeCard.addView(tvLevel)
                                colText.addView(tvLocation)
                                colText.addView(tvTime)

                                rowInner.addView(badgeCard)
                                rowInner.addView(colText)

                                rowOuter.addView(leftBorder)
                                rowOuter.addView(rowInner)

                                outerCard.addView(rowOuter)
                                llContactContainer.addView(outerCard)
                            }
                        }
                    } else {
                        Log.e("LichSuTruyVet", "Lỗi lấy lịch sử tiếp xúc: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LichSuTruyVet", "Lỗi kết nối lịch sử tiếp xúc: ${e.message}")
                }
            }
        }
    }
}