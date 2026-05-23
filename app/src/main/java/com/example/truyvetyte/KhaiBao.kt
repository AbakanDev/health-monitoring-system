package com.example.truyvetyte

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.model.KhaiBaoRequest
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhaiBao : Fragment() {

    // Lịch sử
    private lateinit var tvChuaKhaiBao: TextView
    private lateinit var layoutKhaiBaoContainer: LinearLayout

    // Triệu chứng
    private lateinit var cbSot: CheckBox
    private lateinit var cbKhoTho: CheckBox
    private lateinit var cbDauHong: CheckBox
    private lateinit var cbMetMoi: CheckBox
    private lateinit var cbMatViGiac: CheckBox
    private lateinit var cbHo: CheckBox
    private lateinit var cbKhongTrieuChung: CheckBox

    // Bệnh nền
    private lateinit var rgCoBenhNen: RadioGroup
    private lateinit var etChiTietBenhNen: EditText

    // Đánh giá nguy cơ
    private lateinit var rgTiepXucF0: RadioGroup
    private lateinit var rgDiVeTuVungDich: RadioGroup

    // Cam kết & nút
    private lateinit var cbCamKet: CheckBox
    private lateinit var btnHoanTat: Button

    // Map mã triệu chứng — phải trùng với DB
    private val maTrieuChungMap = mapOf(
        "cbSot"           to "TC001",
        "cbKhoTho"        to "TC002",
        "cbDauHong"       to "TC003",
        "cbMetMoi"        to "TC004",
        "cbMatViGiac"     to "TC005",
        "cbHo"            to "TC006"
        // cbKhongTrieuChung không gửi mã
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.initial_health_declaration_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ánh xạ lịch sử
        tvChuaKhaiBao       = view.findViewById(R.id.tvChuaKhaiBao)
        layoutKhaiBaoContainer = view.findViewById(R.id.layoutKhaiBaoContainer)

        // Ánh xạ form
        cbSot               = view.findViewById(R.id.cbSot)
        cbKhoTho            = view.findViewById(R.id.cbKhoTho)
        cbDauHong           = view.findViewById(R.id.cbDauHong)
        cbMetMoi            = view.findViewById(R.id.cbMetMoi)
        cbMatViGiac         = view.findViewById(R.id.cbMatViGiac)
        cbHo                = view.findViewById(R.id.cbHo)
        cbKhongTrieuChung   = view.findViewById(R.id.cbKhongTrieuChung)
        rgCoBenhNen         = view.findViewById(R.id.rgCoBenhNen)
        etChiTietBenhNen    = view.findViewById(R.id.etChiTietBenhNen)
        rgTiepXucF0         = view.findViewById(R.id.rgTiepXucF0)
        rgDiVeTuVungDich    = view.findViewById(R.id.rgDiVeTuVungDich)
        cbCamKet            = view.findViewById(R.id.cbCamKet)
        btnHoanTat          = view.findViewById(R.id.btnHoanTat1)

        // Nếu tick "Không có triệu chứng" → uncheck hết cái còn lại
        cbKhongTrieuChung.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbSot.isChecked      = false
                cbKhoTho.isChecked   = false
                cbDauHong.isChecked  = false
                cbMetMoi.isChecked   = false
                cbMatViGiac.isChecked = false
                cbHo.isChecked       = false
            }
        }

        // Nếu tick bất kỳ triệu chứng → uncheck "Không có triệu chứng"
        listOf(cbSot, cbKhoTho, cbDauHong, cbMetMoi, cbMatViGiac, cbHo).forEach { cb ->
            cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) cbKhongTrieuChung.isChecked = false
            }
        }

        // Nút Hoàn tất
        btnHoanTat.setOnClickListener { submitKhaiBao() }

        // Gọi API lịch sử
        tvChuaKhaiBao.visibility = View.GONE
        fetchKhaiBaoHistory()
    }

    // ─── SUBMIT ────────────────────────────────────────────────────────────────

    private fun submitKhaiBao() {
        // Validate cam kết
        if (!cbCamKet.isChecked) {
            Toast.makeText(requireContext(), "Vui lòng xác nhận cam kết!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate RadioGroup bắt buộc
        if (rgTiepXucF0.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Vui lòng chọn: tiếp xúc F0", Toast.LENGTH_SHORT).show()
            return
        }
        if (rgDiVeTuVungDich.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Vui lòng chọn: đi về từ vùng dịch", Toast.LENGTH_SHORT).show()
            return
        }
        if (rgCoBenhNen.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Vui lòng chọn: có bệnh nền hay không", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy giá trị
        val tiepXucF0      = if (rgTiepXucF0.checkedRadioButtonId == R.id.rbTiepXucF0Co) 1 else 0
        val coBenhNen      = if (rgCoBenhNen.checkedRadioButtonId == R.id.rbCoBenhNenCo) 1 else 0
        val diVeTuVungDich = if (rgDiVeTuVungDich.checkedRadioButtonId == R.id.rbDiVeCo) 1 else 0
        val chiTietBenhNen = etChiTietBenhNen.text.toString().trim().ifEmpty { null }

        // Lấy danh sách triệu chứng
        val danhSachTrieuChung = mutableListOf<String>()
        if (cbSot.isChecked)      danhSachTrieuChung.add("TC001")
        if (cbKhoTho.isChecked)   danhSachTrieuChung.add("TC002")
        if (cbDauHong.isChecked)  danhSachTrieuChung.add("TC003")
        if (cbMetMoi.isChecked)   danhSachTrieuChung.add("TC004")
        if (cbMatViGiac.isChecked)danhSachTrieuChung.add("TC005")
        if (cbHo.isChecked)       danhSachTrieuChung.add("TC006")
        // cbKhongTrieuChung → gửi list rỗng, không thêm mã

        val request = KhaiBaoRequest(
            TiepXucF0      = tiepXucF0,
            CoBenhNen      = coBenhNen,
            ChiTietBenhNen = chiTietBenhNen,
            DiVeTuVungDich = diVeTuVungDich,
            danhSachTrieuChung = danhSachTrieuChung
        )

        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
            return
        }

        btnHoanTat.isEnabled = false
        btnHoanTat.text = "Đang gửi..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.submitKhaiBao("Bearer $token", request)

                withContext(Dispatchers.Main) {
                    btnHoanTat.isEnabled = true
                    btnHoanTat.text = "HOÀN TẤT"

                    if (response.isSuccessful && response.body()?.success == true) {
                        // Chuyển sang màn chúc mừng
                        val intent = Intent(requireContext(), ChucMungKhaiBao::class.java)
                        startActivity(intent)
                        // Reload lịch sử sau khi submit
                        fetchKhaiBaoHistory()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Khai báo thất bại: ${response.body()?.message ?: "Lỗi server"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnHoanTat.isEnabled = true
                    btnHoanTat.text = "HOÀN TẤT"
                    Log.e("KhaiBao", "Lỗi submit: ${e.message}")
                    Toast.makeText(requireContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ─── LỊCH SỬ ───────────────────────────────────────────────────────────────

    private fun fetchKhaiBaoHistory() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd  = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getKhaiBaoHistory("Bearer $token", cccd)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        if (body != null && body.success && !body.data.isNullOrEmpty()) {
                            tvChuaKhaiBao.visibility = View.GONE
                            layoutKhaiBaoContainer.removeAllViews()

                            body.data.forEachIndexed { index, item ->

                                if (index > 0) {
                                    val divider = View(requireContext()).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                                        ).also { p ->
                                            p.marginStart = 16.dpToPx()
                                            p.marginEnd   = 16.dpToPx()
                                        }
                                        setBackgroundColor(0xFFE0E0E0.toInt())
                                    }
                                    layoutKhaiBaoContainer.addView(divider)
                                }

                                val row = RelativeLayout(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
                                }

                                val lanSo = item.LanKhaiBao.toIntOrNull() ?: (index + 1)

                                val tvLan = TextView(requireContext()).apply {
                                    id = View.generateViewId()
                                    text = "Lần $lanSo"
                                    textSize = 20f
                                    setTextColor(0xFF79A9F5.toInt())
                                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                                    layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                    ).also { p ->
                                        p.addRule(RelativeLayout.ALIGN_PARENT_START)
                                        p.addRule(RelativeLayout.CENTER_VERTICAL)
                                    }
                                }

                                val tvNgayGio = TextView(requireContext()).apply {
                                    text = "${item.Ngay} - ${item.Gio}"
                                    textSize = 14f
                                    setTextColor(0xFF92BEFA.toInt())
                                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                                    layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                    ).also { p ->
                                        p.addRule(RelativeLayout.ALIGN_PARENT_END)
                                        p.addRule(RelativeLayout.CENTER_VERTICAL)
                                    }
                                }

                                row.addView(tvLan)
                                row.addView(tvNgayGio)
                                layoutKhaiBaoContainer.addView(row)
                            }

                        } else {
                            tvChuaKhaiBao.visibility = View.VISIBLE
                            layoutKhaiBaoContainer.removeAllViews()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("KhaiBao", "Lỗi lịch sử: ${e.message}")
            }
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}