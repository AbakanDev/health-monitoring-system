package com.example.truyvetyte

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DichTe : Fragment() {

    // --- Khai báo View Tiêm Chủng ---
    private lateinit var cvTheTiemChung: CardView
    private lateinit var tvLoaiThe: TextView
    private lateinit var tvSoMuiTiem: TextView
    private lateinit var tvChuaTiemChung: TextView

    private lateinit var layoutMui1: RelativeLayout
    private lateinit var tvTenVaccineMui1: TextView
    private lateinit var tvDiaDiemMui1: TextView
    private lateinit var tvNgayTiemMui1: TextView

    private lateinit var layoutMui2: RelativeLayout
    private lateinit var tvTenVaccineMui2: TextView
    private lateinit var tvDiaDiemMui2: TextView
    private lateinit var tvNgayTiemMui2: TextView
    private lateinit var dividerTiemChung: View

    // --- Khai báo View Cách Ly ---
    private lateinit var viewProgressCachLy: View
    private lateinit var viewProgressCachLyRemain: View
    private lateinit var tvNgayBatDauCachLy: TextView
    private lateinit var tvNgayKetThucCachLy: TextView
    private lateinit var tvSoNgayConLai: TextView

    // --- Khai báo View Xét Nghiệm (Bổ sung để đủ ID) ---
    private lateinit var tvTrangThaiXetNghiem: TextView
    private lateinit var tvLanXetNghiemMoiNhat: TextView
    private lateinit var tvNgayXetNghiemMoiNhat: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.public_health_management_screen, container, false)

        // Ánh xạ View
        initViews(view)

        // Gọi API lấy dữ liệu tiêm chủng
        fetchTiemChungData()
        fetchCachLyData()

        return view
    }

    private fun initViews(view: View) {
        // Tiêm chủng
        cvTheTiemChung = view.findViewById(R.id.cvTheTiemChung)
        tvLoaiThe = view.findViewById(R.id.tvLoaiThe)
        tvSoMuiTiem = view.findViewById(R.id.tvSoMuiTiem)
        tvChuaTiemChung = view.findViewById(R.id.tvChuaTiemChung)

        layoutMui1 = view.findViewById(R.id.layoutMui1)
        tvTenVaccineMui1 = view.findViewById(R.id.tvTenVaccineMui1)
        tvDiaDiemMui1 = view.findViewById(R.id.tvDiaDiemMui1)
        tvNgayTiemMui1 = view.findViewById(R.id.tvNgayTiemMui1)

        layoutMui2 = view.findViewById(R.id.layoutMui2)
        tvTenVaccineMui2 = view.findViewById(R.id.tvTenVaccineMui2)
        tvDiaDiemMui2 = view.findViewById(R.id.tvDiaDiemMui2)
        tvNgayTiemMui2 = view.findViewById(R.id.tvNgayTiemMui2)
        dividerTiemChung = view.findViewById(R.id.dividerTiemChung)

        // Cách ly
        viewProgressCachLy = view.findViewById(R.id.viewProgressCachLy)
        viewProgressCachLyRemain = view.findViewById(R.id.viewProgressCachLyRemain)
        tvNgayBatDauCachLy = view.findViewById(R.id.tvNgayBatDauCachLy)
        tvNgayKetThucCachLy = view.findViewById(R.id.tvNgayKetThucCachLy)
        tvSoNgayConLai = view.findViewById(R.id.tvSoNgayConLai)

        // Xét nghiệm
        tvTrangThaiXetNghiem = view.findViewById(R.id.tvTrangThaiXetNghiem)
        tvLanXetNghiemMoiNhat = view.findViewById(R.id.tvLanXetNghiemMoiNhat)
        tvNgayXetNghiemMoiNhat = view.findViewById(R.id.tvNgayXetNghiemMoiNhat)
    }

    // ================= LOGIC XỬ LÝ CÁCH LY =================
    private fun updateTrangThaiCachLy(ngayBatDauStr: String, ngayKetThucStr: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val ngayBatDau = sdf.parse(ngayBatDauStr)
            val ngayKetThuc = sdf.parse(ngayKetThucStr)
            val homNay = Date() // Lấy thời gian hiện tại lúc mở app

            if (ngayBatDau != null && ngayKetThuc != null) {
                val totalTime = ngayKetThuc.time - ngayBatDau.time
                val passedTime = homNay.time - ngayBatDau.time

                val totalDays = TimeUnit.MILLISECONDS.toDays(totalTime).toFloat()
                val passedDays = TimeUnit.MILLISECONDS.toDays(passedTime).toFloat()

                // Tính phần trăm tiến độ
                var percent = (passedDays / totalDays) * 100f
                if (percent < 0f) percent = 0f
                if (percent > 100f) percent = 100f

                val remainDays = (totalDays - passedDays).toInt()

                // Cập nhật text ngày tháng
                tvNgayBatDauCachLy.text = ngayBatDauStr
                tvNgayKetThucCachLy.text = ngayKetThucStr

                // Cập nhật trạng thái số ngày
                if (remainDays > 0) {
                    tvSoNgayConLai.text = "Còn $remainDays ngày còn lại"
                    tvSoNgayConLai.setTextColor(Color.parseColor("#E4C15A")) // Giữ màu vàng
                } else if (remainDays == 0) {
                    tvSoNgayConLai.text = "Ngày cuối cùng cách ly"
                    tvSoNgayConLai.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    tvSoNgayConLai.text = "Đã hoàn thành cách ly"
                    tvSoNgayConLai.setTextColor(Color.parseColor("#4CAF50")) // Đổi xanh khi xong
                    percent = 100f
                }

                // Cập nhật độ dài của thanh Progress giả lập bằng layout_weight
                val paramsProgress = viewProgressCachLy.layoutParams as LinearLayout.LayoutParams
                paramsProgress.weight = percent
                viewProgressCachLy.layoutParams = paramsProgress

                val paramsRemain = viewProgressCachLyRemain.layoutParams as LinearLayout.LayoutParams
                paramsRemain.weight = 100f - percent
                viewProgressCachLyRemain.layoutParams = paramsRemain
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Xử lý lỗi parse ngày nếu sai format
        }
    }

    // ================= LOGIC XỬ LÝ TIÊM CHỦNG (Giữ nguyên) =================
    private fun fetchTiemChungData() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy Token hoặc CCCD. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
            return
        }

        val bearerToken = "Bearer $token"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getThongTinTiemChung(bearerToken, cccd)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        updateUI(data.soMuiTiem, data.loaiThe, data.danhSachTiem)
                    } else {
                        Toast.makeText(requireContext(), "Không lấy được dữ liệu tiêm chủng!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchCachLyData() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) return

        val bearerToken = "Bearer $token"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Gọi API thông qua Retrofit
                val response = RetrofitClient.instance.getThongTinCachLy(bearerToken, cccd)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val cachLyResponse = response.body()!!

                        // Kiểm tra flag isQuarantined từ backend và mảng data có dữ liệu không
                        if (cachLyResponse.success && cachLyResponse.isQuarantined && cachLyResponse.data.isNotEmpty()) {
                            val activeCachLy = cachLyResponse.data[0]
                            val ngayBatDau = activeCachLy.NgayBatDau
                            val ngayKetThuc = activeCachLy.NgayKetThuc

                            if (ngayBatDau != null && ngayKetThuc != null) {
                                updateTrangThaiCachLy(ngayBatDau, ngayKetThuc)
                            }
                        } else {
                            // Xử lý UI khi người dùng KHÔNG bị cách ly hoặc mảng data rỗng
                            tvNgayBatDauCachLy.text = "--/--/----"
                            tvNgayKetThucCachLy.text = "--/--/----"
                            tvSoNgayConLai.text = "Không có yêu cầu cách ly"
                            tvSoNgayConLai.setTextColor(Color.parseColor("#4CAF50"))

                            // Đẩy thanh progress về 0
                            val paramsProgress = viewProgressCachLy.layoutParams as LinearLayout.LayoutParams
                            paramsProgress.weight = 0f
                            viewProgressCachLy.layoutParams = paramsProgress

                            val paramsRemain = viewProgressCachLyRemain.layoutParams as LinearLayout.LayoutParams
                            paramsRemain.weight = 100f
                            viewProgressCachLyRemain.layoutParams = paramsRemain
                        }
                    } else {
                        Toast.makeText(requireContext(), "Không lấy được dữ liệu cách ly!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(soMuiTiem: Int, loaiThe: String, danhSach: List<com.example.truyvetyte.model.ChiTietTiem>?) {
        tvSoMuiTiem.text = soMuiTiem.toString()
        when (loaiThe.uppercase()) {
            "XANH" -> {
                cvTheTiemChung.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                tvLoaiThe.text = "THẺ XANH"
            }
            "VANG" -> {
                cvTheTiemChung.setCardBackgroundColor(Color.parseColor("#FFC107"))
                tvLoaiThe.text = "THẺ VÀNG"
            }
            "DO" -> {
                cvTheTiemChung.setCardBackgroundColor(Color.parseColor("#F44336"))
                tvLoaiThe.text = "THẺ ĐỎ"
            }
        }

        layoutMui1.visibility = View.GONE
        layoutMui2.visibility = View.GONE
        dividerTiemChung.visibility = View.GONE
        tvChuaTiemChung.visibility = View.GONE

        if (soMuiTiem == 0 || danhSach.isNullOrEmpty()) {
            tvChuaTiemChung.visibility = View.VISIBLE
        } else {
            danhSach.forEach { muiTiem ->
                if (muiTiem.muiSo == 1) {
                    layoutMui1.visibility = View.VISIBLE
                    tvTenVaccineMui1.text = muiTiem.tenVaccine
                    tvDiaDiemMui1.text = muiTiem.diaDiem
                    tvNgayTiemMui1.text = muiTiem.ngayTiem
                } else if (muiTiem.muiSo == 2) {
                    layoutMui2.visibility = View.VISIBLE
                    dividerTiemChung.visibility = View.VISIBLE
                    tvTenVaccineMui2.text = muiTiem.tenVaccine
                    tvDiaDiemMui2.text = muiTiem.diaDiem
                    tvNgayTiemMui2.text = muiTiem.ngayTiem
                }
            }
        }
    }
}