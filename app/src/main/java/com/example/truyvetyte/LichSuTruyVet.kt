package com.example.truyvetyte

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
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

    // Views cho Lịch Sử Tiếp Xúc - Item 1
    private lateinit var cvContactItem1: CardView
    private lateinit var tvContactLevel1: TextView
    private lateinit var tvContactLocation1: TextView
    private lateinit var tvContactTime1: TextView

    // Views cho Lịch Sử Tiếp Xúc - Item 2
    private lateinit var cvContactItem2: CardView
    private lateinit var tvContactLevel2: TextView
    private lateinit var tvContactLocation2: TextView
    private lateinit var tvContactTime2: TextView

    // Views cho Tổng Quan Khu Vực
    private lateinit var tvDangerAreaCount: TextView
    private lateinit var tvRiskAreaCount: TextView
    private lateinit var tvSafeAreaCount: TextView
    private lateinit var tvTotalCheckInCount: TextView

    // Views cho Lịch Sử Check-In (Sử dụng mảng để dễ quản lý dữ liệu lặp)
    private val cvCheckInItems = mutableListOf<CardView>()
    private val tvCheckInLocations = mutableListOf<TextView>()
    private val tvCheckInTimes = mutableListOf<TextView>()
    private val tvCheckInStatuses = mutableListOf<TextView>()

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

        // 2. Ánh xạ View Lịch Sử Tiếp Xúc
        cvContactItem1 = view.findViewById(R.id.cvContactItem1)
        tvContactLevel1 = view.findViewById(R.id.tvContactLevel1)
        tvContactLocation1 = view.findViewById(R.id.tvContactLocation1)
        tvContactTime1 = view.findViewById(R.id.tvContactTime1)

        cvContactItem2 = view.findViewById(R.id.cvContactItem2)
        tvContactLevel2 = view.findViewById(R.id.tvContactLevel2)
        tvContactLocation2 = view.findViewById(R.id.tvContactLocation2)
        tvContactTime2 = view.findViewById(R.id.tvContactTime2)

        // 3. Ánh xạ View Tổng Quan Khu Vực (Check-in)
        tvDangerAreaCount = view.findViewById(R.id.tvDangerAreaCount)
        tvRiskAreaCount = view.findViewById(R.id.tvRiskAreaCount)
        tvSafeAreaCount = view.findViewById(R.id.tvSafeAreaCount)
        tvTotalCheckInCount = view.findViewById(R.id.tvTotalCheckInCount)

        // 4. Ánh xạ View Lịch Sử Check-In
        cvCheckInItems.add(view.findViewById(R.id.cvCheckInItem1))
        cvCheckInItems.add(view.findViewById(R.id.cvCheckInItem2))
        cvCheckInItems.add(view.findViewById(R.id.cvCheckInItem3))
        cvCheckInItems.add(view.findViewById(R.id.cvCheckInItem4))

        tvCheckInLocations.add(view.findViewById(R.id.tvCheckInLocation1))
        tvCheckInLocations.add(view.findViewById(R.id.tvCheckInLocation2))
        tvCheckInLocations.add(view.findViewById(R.id.tvCheckInLocation3))
        tvCheckInLocations.add(view.findViewById(R.id.tvCheckInLocation4))

        tvCheckInTimes.add(view.findViewById(R.id.tvCheckInTime1))
        tvCheckInTimes.add(view.findViewById(R.id.tvCheckInTime2))
        tvCheckInTimes.add(view.findViewById(R.id.tvCheckInTime3))
        tvCheckInTimes.add(view.findViewById(R.id.tvCheckInTime4))

        tvCheckInStatuses.add(view.findViewById(R.id.tvCheckInStatus1))
        tvCheckInStatuses.add(view.findViewById(R.id.tvCheckInStatus2))
        tvCheckInStatuses.add(view.findViewById(R.id.tvCheckInStatus3))
        tvCheckInStatuses.add(view.findViewById(R.id.tvCheckInStatus4))

        // Ẩn mặc định các item lịch sử check-in
        cvCheckInItems.forEach { it.visibility = View.GONE }

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

                        // Lặp qua danh sách trả về (Tối đa 4 item dựa theo XML của bạn)
                        for (i in 0 until minOf(listData.size, cvCheckInItems.size)) {
                            cvCheckInItems[i].visibility = View.VISIBLE
                            tvCheckInLocations[i].text = listData[i].TenKhuVuc ?: "Không rõ"
                            tvCheckInTimes[i].text = listData[i].ThoiGianCheckIn ?: "Không rõ"

                            val status = listData[i].TrangThaiKhuVuc ?: "An Toàn"

                            // Gắn text theo định dạng xuống dòng giống thiết kế XML (Nguy\nHiểm)
                            tvCheckInStatuses[i].text = status.replace(" ", "\n")

                            // Cài đặt màu sắc dựa trên mức độ khu vực
                            val cardStatusView = tvCheckInStatuses[i].parent as CardView
                            val leftBorderView = (cvCheckInItems[i].getChildAt(0) as ViewGroup).getChildAt(0)

                            when (status) {
                                "Nguy Hiểm" -> {
                                    tvCheckInStatuses[i].setTextColor(Color.parseColor("#C62828"))
                                    cardStatusView.setCardBackgroundColor(Color.parseColor("#EF9A9A"))
                                    leftBorderView.setBackgroundColor(Color.parseColor("#E53935"))
                                }
                                "Nguy Cơ" -> {
                                    tvCheckInStatuses[i].setTextColor(Color.parseColor("#F57F17"))
                                    cardStatusView.setCardBackgroundColor(Color.parseColor("#FFF59D"))
                                    leftBorderView.setBackgroundColor(Color.parseColor("#FFF176"))
                                }
                                "An Toàn" -> {
                                    tvCheckInStatuses[i].setTextColor(Color.parseColor("#2E7D32"))
                                    cardStatusView.setCardBackgroundColor(Color.parseColor("#A5D6A7"))
                                    leftBorderView.setBackgroundColor(Color.parseColor("#81C784"))
                                }
                            }
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

                            // Gán dữ liệu cho Item 1 (nếu có)
                            if (listData.isNotEmpty()) {
                                cvContactItem1.visibility = View.VISIBLE
                                tvContactLevel1.text = listData[0].capDoDichTeHienTai ?: "An Toàn"
                                tvContactLocation1.text = listData[0].diaDiemTiepXuc ?: "Không rõ"
                                tvContactTime1.text = listData[0].thoiGianTiepXuc ?: "Không rõ"
                            }

                            // Gán dữ liệu cho Item 2 (nếu có)
                            if (listData.size > 1) {
                                cvContactItem2.visibility = View.VISIBLE
                                tvContactLevel2.text = listData[1].capDoDichTeHienTai ?: "An Toàn"
                                tvContactLocation2.text = listData[1].diaDiemTiepXuc ?: "Không rõ"
                                tvContactTime2.text = listData[1].thoiGianTiepXuc ?: "Không rõ"
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