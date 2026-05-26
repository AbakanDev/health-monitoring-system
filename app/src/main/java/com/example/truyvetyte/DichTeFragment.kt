package com.example.truyvetyte

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DichTeFragment : Fragment() {

    // Khai báo các TextView
    private lateinit var tvTongCaBenh: TextView
    private lateinit var tvTongBenhNhan: TextView
    private lateinit var tvTiemChung: TextView
    private lateinit var tvXetNghiem: TextView
    private lateinit var tvCachLy: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.medical_activity_dashboard, container, false)

        // Ánh xạ layout
        tvTongCaBenh = view.findViewById(R.id.tvTongCaBenh)
        tvTongBenhNhan = view.findViewById(R.id.tvTongBenhNhan)
        tvTiemChung = view.findViewById(R.id.tvTiemChung)
        tvXetNghiem = view.findViewById(R.id.tvXetNghiem)
        tvCachLy = view.findViewById(R.id.tvCachLy)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gọi trực tiếp hàm giống như bên XuHuong.kt
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Đưa việc lấy Token vào đây
                val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", "") ?: ""

                if (token.isEmpty()) {
                    Toast.makeText(requireContext(), "Lỗi: Không tìm thấy Token!", Toast.LENGTH_SHORT).show()
                    return@launch // Dừng thực thi nếu không có token
                }

                val formattedToken = if (token.startsWith("Bearer")) token else "Bearer $token"

                // Bắt đầu gọi API
                val response = RetrofitClient.instance.getDashboardOverview(formattedToken)

                if (response.isSuccessful && response.body() != null) {
                    val overviewResponse = response.body()!!

                    if (overviewResponse.success && overviewResponse.data != null) {
                        val data = overviewResponse.data

                        // Format số liệu thành dạng có dấu chấm (VD: 1.000)
                        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

                        tvTongCaBenh.text = formatter.format(data.TongCaBenh)
                        tvTongBenhNhan.text = formatter.format(data.TongBenhNhan)
                        tvTiemChung.text = formatter.format(data.TiemChungThangNay)
                        tvXetNghiem.text = formatter.format(data.XetNghiemThangNay)
                        tvCachLy.text = formatter.format(data.CachLyThangNay)
                    } else {
                        Log.e("DichTeFragment", overviewResponse.message)
                    }
                } else {
                    Log.e("DichTeFragment", "Lỗi lấy dữ liệu từ server")
                }
            } catch (e: Exception) {
                Log.e("DichTeFragment", "Lỗi kết nối dashboard overview: ${e.message}")
            }
        }
    }
}