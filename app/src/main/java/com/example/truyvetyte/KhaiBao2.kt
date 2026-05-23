package com.example.truyvetyte

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.model.CuaKhauItem
import com.example.truyvetyte.model.ImmigrationHistoryItem
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhaiBao2 : Fragment() {

    // ── Views ──────────────────────────────────────────────────────────────────
    private lateinit var tvChuaXuatNhapCanh: TextView
    private lateinit var layoutXuatNhapCanhContainer: LinearLayout
    private lateinit var rgLoaiCuaKhau: RadioGroup
    private lateinit var spnChonCuaKhau: Spinner
    private lateinit var cbKhongCoCuaKhau: CheckBox
    private lateinit var cbCamKetXNC: CheckBox
    private lateinit var btnHoanTat2: Button

    // ── State ──────────────────────────────────────────────────────────────────
    /** Toàn bộ danh sách cửa khẩu (đã hardcode) */
    private var allCuaKhau: List<CuaKhauItem> = emptyList()

    /** Danh sách đang hiển thị trong Spinner (lọc theo loại) */
    private var filteredCuaKhau: List<CuaKhauItem> = emptyList()

    /** Cửa khẩu người dùng đang chọn */
    private var selectedMaCuaKhau: String? = null

    // ── Map: RadioButton id → LoaiCuaKhau ────────────────────────────
    // DB dùng: "Đường hàng không" | "Đường bộ" | "Đường biển"
    private val loaiMap = mapOf(
        R.id.rbHangKhong  to "Đường hàng không",
        R.id.rbDatLien    to "Đường bộ",
        R.id.rbDuongBien  to "Đường biển"
    )

    // ──────────────────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.initial_immigration_declaration_screen,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupListeners()

        // Ẩn "chưa có dữ liệu" khi đang load
        tvChuaXuatNhapCanh.visibility = View.GONE

        fetchImmigrationHistory()
        fetchCuaKhauList() // Gọi hàm hardcode dữ liệu
    }

    // ── Bind ───────────────────────────────────────────────────────────────────

    private fun bindViews(view: View) {
        tvChuaXuatNhapCanh         = view.findViewById(R.id.tvChuaXuatNhapCanh)
        layoutXuatNhapCanhContainer = view.findViewById(R.id.layoutXuatNhapCanhContainer)
        rgLoaiCuaKhau              = view.findViewById(R.id.rgLoaiCuaKhau)
        spnChonCuaKhau             = view.findViewById(R.id.spnChonCuaKhau)
        cbKhongCoCuaKhau           = view.findViewById(R.id.cbKhongCoCuaKhau)
        cbCamKetXNC                = view.findViewById(R.id.cbCamKetXNC)
        btnHoanTat2                = view.findViewById(R.id.btnHoanTat2)
    }

    // ── Listeners ──────────────────────────────────────────────────────────────

    private fun setupListeners() {

        // 1. Khi chọn Loại cửa khẩu → lọc Spinner
        rgLoaiCuaKhau.setOnCheckedChangeListener { _, checkedId ->
            val loai = loaiMap[checkedId] ?: return@setOnCheckedChangeListener
            filterSpinnerByLoai(loai)

            // Bỏ check "không có cửa khẩu" nếu đang chọn loại
            cbKhongCoCuaKhau.isChecked = false
            spnChonCuaKhau.isEnabled = true
        }

        // 2. Checkbox "không có cửa khẩu" → vô hiệu hoá spinner & radio
        cbKhongCoCuaKhau.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rgLoaiCuaKhau.clearCheck()
                spnChonCuaKhau.isEnabled = false
                selectedMaCuaKhau = null
                clearSpinner()
            } else {
                spnChonCuaKhau.isEnabled = true
            }
        }

        // 3. Hoàn tất
        btnHoanTat2.setOnClickListener {
            if (!validateAndSubmit()) return@setOnClickListener
        }
    }

    // ── Spinner helpers ────────────────────────────────────────────────────────

    /**
     * Lọc danh sách cửa khẩu theo loại rồi bind vào Spinner.
     * [loai] khớp với giá trị LoaiCuaKhau trong list.
     */
    private fun filterSpinnerByLoai(loai: String) {
        filteredCuaKhau = allCuaKhau.filter {
            it.loaiCuaKhau.trim().equals(loai.trim(), ignoreCase = true)
        }
        bindSpinner(filteredCuaKhau)
    }

    private fun bindSpinner(list: List<CuaKhauItem>) {
        if (list.isEmpty()) {
            val emptyMessage = if (allCuaKhau.isEmpty()) {
                "-- Đang tải dữ liệu cửa khẩu... --"
            } else {
                "-- Không có cửa khẩu thuộc loại này --"
            }

            spnChonCuaKhau.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf(emptyMessage)
            )
            selectedMaCuaKhau = null
            return
        }

        // Hiển thị TenCuaKhau trong dropdown
        val displayNames = list.map { it.tenCuaKhau }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            displayNames
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spnChonCuaKhau.adapter = adapter
        selectedMaCuaKhau = list[0].maCuaKhau  // mặc định chọn item đầu

        spnChonCuaKhau.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedMaCuaKhau = list[pos].maCuaKhau
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMaCuaKhau = null
            }
        }
    }

    private fun clearSpinner() {
        spnChonCuaKhau.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("-- Chọn loại cửa khẩu trước --")
        )
        selectedMaCuaKhau = null
    }

    // ── Validate & Submit ──────────────────────────────────────────────────────

    private fun validateAndSubmit(): Boolean {
        // Phải chọn loại hoặc tick "không có cửa khẩu"
        if (rgLoaiCuaKhau.checkedRadioButtonId == -1 && !cbKhongCoCuaKhau.isChecked) {
            Toast.makeText(requireContext(), "Vui lòng chọn loại cửa khẩu", Toast.LENGTH_SHORT).show()
            return false
        }

        // Nếu đã chọn loại thì phải có cửa khẩu cụ thể
        if (!cbKhongCoCuaKhau.isChecked && selectedMaCuaKhau == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn cửa khẩu cụ thể", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!cbCamKetXNC.isChecked) {
            Toast.makeText(requireContext(), "Vui lòng xác nhận cam kết thông tin", Toast.LENGTH_SHORT).show()
            return false
        }

        // Nếu không có cửa khẩu → bỏ qua submit API, đi thẳng đến màn Chúc Mừng
        if (cbKhongCoCuaKhau.isChecked) {
            navigateToSuccess()
            return true
        }

        submitImmigrationDeclaration()
        return true
    }

    private fun navigateToSuccess() {
        startActivity(Intent(requireContext(), ChucMungKhaiBao::class.java))
    }

    // ── Dữ liệu nội bộ: Danh sách cửa khẩu ──────────────────────────────────────

    private fun fetchCuaKhauList() {
        // Hardcode danh sách trực tiếp vào allCuaKhau
        // Đảm bảo data class CuaKhauItem của bạn có đúng thứ tự tham số:
        // (maCuaKhau, tenCuaKhau, loaiCuaKhau, trangThai)
        allCuaKhau = listOf(
            CuaKhauItem("CK01", "Cửa khẩu Quốc tế Nội Bài", "Đường hàng không", "Đang hoạt động"),
            CuaKhauItem("CK03", "Cửa khẩu Quốc tế Tân Sơn Nhất", "Đường hàng không", "Đang hoạt động"),
            CuaKhauItem("CK04", "Cửa khẩu Quốc tế Đà Nẵng", "Đường hàng không", "Đang hoạt động"),
            CuaKhauItem("CK05", "Cửa khẩu Lào Cai", "Đường bộ", "Đang hoạt động"),
            CuaKhauItem("CK06", "Cửa khẩu Hữu Nghị", "Đường bộ", "Đang hoạt động"),
            CuaKhauItem("CK07", "Cửa khẩu Lao Bảo", "Đường bộ", "Đang hoạt động"),
            CuaKhauItem("CK02", "Cửa khẩu Mộc Bài", "Đường bộ", "Đang hoạt động"),
            CuaKhauItem("CK08", "Cảng Quốc tế Hải Phòng", "Đường biển", "Đang hoạt động"),
            CuaKhauItem("CK09", "Cảng Quốc tế Sài Gòn (SPCT)", "Đường biển", "Đang hoạt động")
        )

        val checkedId = rgLoaiCuaKhau.checkedRadioButtonId
        if (checkedId != -1) {
            loaiMap[checkedId]?.let { filterSpinnerByLoai(it) }
        } else {
            clearSpinner()
        }
    }

    // ── API: Submit tờ khai XNC ────────────────────────────────────────────────

    private fun submitImmigrationDeclaration() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null) ?: return

        val maCuaKhau = selectedMaCuaKhau ?: return

        btnHoanTat2.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.submitImmigrationDeclaration(
                    "Bearer $token",
                    mapOf("MaCuaKhau" to maCuaKhau)
                )
                withContext(Dispatchers.Main) {
                    btnHoanTat2.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Tùy chọn: Bạn có thể lấy response.body()?.data?.MaToKhaiXNC ở đây nếu cần lưu lại
                        navigateToSuccess()
                    } else {
                        Toast.makeText(requireContext(), "Khai báo thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("KhaiBao2", "submitImmigrationDeclaration error: ${e.message}")
                withContext(Dispatchers.Main) {
                    btnHoanTat2.isEnabled = true
                    Toast.makeText(requireContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ── API: Lịch sử XNC (giữ nguyên logic cũ) ────────────────────────────────

    private fun fetchImmigrationHistory() {
        val prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("TOKEN", null)
        val cccd  = prefs.getString("CCCD", null)

        if (token == null || cccd == null) {
            tvChuaXuatNhapCanh.visibility = View.VISIBLE
            tvChuaXuatNhapCanh.text = "Không tìm thấy thông tin người dùng"
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getImmigrationHistory("Bearer $token", cccd)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success && !body.data.isNullOrEmpty()) {
                            tvChuaXuatNhapCanh.visibility = View.GONE
                            layoutXuatNhapCanhContainer.removeAllViews()
                            body.data.forEachIndexed { index, item ->
                                if (index > 0) layoutXuatNhapCanhContainer.addView(buildDivider())
                                layoutXuatNhapCanhContainer.addView(buildHistoryRow(item))
                            }
                        } else {
                            tvChuaXuatNhapCanh.visibility = View.VISIBLE
                            tvChuaXuatNhapCanh.text = "Bạn chưa có lịch sử xuất nhập cảnh"
                            layoutXuatNhapCanhContainer.removeAllViews()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("KhaiBao2", "fetchImmigrationHistory error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ── View builders ──────────────────────────────────────────────────────────

    private fun buildDivider(): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ).also { p -> p.marginStart = 16.dpToPx(); p.marginEnd = 16.dpToPx() }
        setBackgroundColor(0xFFE0E0E0.toInt())
    }

    private fun buildHistoryRow(item: ImmigrationHistoryItem): RelativeLayout {
        val row = RelativeLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
        }

        val tvCuaKhau = TextView(requireContext()).apply {
            id = View.generateViewId()
            text = "📍 ${item.tenCuaKhau} (${item.loaiCuaKhau})"
            textSize = 18f
            setTextColor(0xFF79A9F5.toInt())
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also { it.addRule(RelativeLayout.ALIGN_PARENT_START) }
        }

        val tvThoiGian = TextView(requireContext()).apply {
            id = View.generateViewId()
            text = "${item.ngay} - ${item.gio}"
            textSize = 14f
            setTextColor(0xFF92BEFA.toInt())
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also { p -> p.addRule(RelativeLayout.BELOW, tvCuaKhau.id); p.topMargin = 6.dpToPx() }
        }

        val tvTrangThai = TextView(requireContext()).apply {
            text = "Trạng thái: ${item.trangThaiCuaKhau}"
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also { p ->
                p.addRule(RelativeLayout.ALIGN_PARENT_END)
                p.addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        row.addView(tvCuaKhau)
        row.addView(tvThoiGian)
        row.addView(tvTrangThai)
        return row
    }

    // ── Utils ──────────────────────────────────────────────────────────────────

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()
}