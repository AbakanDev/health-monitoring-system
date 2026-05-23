package com.example.truyvetyte

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhaiBao : Fragment() {

    private lateinit var tvChuaKhaiBao: TextView
    private lateinit var layoutKhaiBaoContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.initial_health_declaration_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ánh xạ View
        tvChuaKhaiBao = view.findViewById(R.id.tvChuaKhaiBao)
        layoutKhaiBaoContainer = view.findViewById(R.id.layoutKhaiBaoContainer)

        // Ẩn mặc định
        tvChuaKhaiBao.visibility = View.GONE
        layoutKhaiBaoContainer.removeAllViews()

        // 2. Nút Hoàn tất
        val btnGo = view.findViewById<Button>(R.id.btnHoanTat1)
        btnGo.setOnClickListener {
            val intent = Intent(requireContext(), ChucMungKhaiBao::class.java)
            startActivity(intent)
        }

        // 3. Gọi API lịch sử khai báo
        fetchKhaiBaoHistory()
    }

    private fun fetchKhaiBaoHistory() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        val cccd = sharedPreferences.getString("CCCD", null)

        if (token == null || cccd == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getKhaiBaoHistory("Bearer $token", cccd)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        if (body != null && body.success && !body.data.isNullOrEmpty()) {
                            tvChuaKhaiBao.visibility = View.GONE
                            layoutKhaiBaoContainer.removeAllViews()

                            // API trả DESC (mới nhất index 0),
                            // forEachIndexed để biết vị trí thêm divider
                            body.data.forEachIndexed { index, item ->

                                // Divider (trừ item đầu tiên)
                                if (index > 0) {
                                    val divider = View(requireContext()).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                                        ).also { params ->
                                            params.marginStart = 16.dpToPx()
                                            params.marginEnd = 16.dpToPx()
                                        }
                                        setBackgroundColor(0xFFE0E0E0.toInt())
                                    }
                                    layoutKhaiBaoContainer.addView(divider)
                                }

                                // Row chứa "Lần X" và ngày giờ
                                val row = RelativeLayout(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    setPadding(
                                        16.dpToPx(),
                                        12.dpToPx(),
                                        16.dpToPx(),
                                        12.dpToPx()
                                    )
                                }

                                // Ép kiểu String → Int, fallback về index + 1 nếu lỗi
                                val lanSo = item.LanKhaiBao.toIntOrNull() ?: (index + 1)

                                // TextView "Lần X"
                                val tvLanId = View.generateViewId()
                                val tvLan = TextView(requireContext()).apply {
                                    id = tvLanId
                                    text = "Lần $lanSo"
                                    textSize = 20f
                                    setTextColor(0xFF79A9F5.toInt())
                                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                                    layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                    ).also { params ->
                                        params.addRule(RelativeLayout.ALIGN_PARENT_START)
                                        params.addRule(RelativeLayout.CENTER_VERTICAL)
                                    }
                                }

                                // TextView ngày - giờ
                                val tvNgayGio = TextView(requireContext()).apply {
                                    text = "${item.Ngay} - ${item.Gio}"
                                    textSize = 14f
                                    setTextColor(0xFF92BEFA.toInt())
                                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                                    layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                    ).also { params ->
                                        params.addRule(RelativeLayout.ALIGN_PARENT_END)
                                        params.addRule(RelativeLayout.CENTER_VERTICAL)
                                    }
                                }

                                row.addView(tvLan)
                                row.addView(tvNgayGio)
                                layoutKhaiBaoContainer.addView(row)
                            }

                        } else {
                            // Không có data
                            tvChuaKhaiBao.visibility = View.VISIBLE
                            layoutKhaiBaoContainer.removeAllViews()
                        }

                    } else {
                        Log.e("KhaiBao", "Lỗi Server: ${response.code()}")
                        tvChuaKhaiBao.visibility = View.VISIBLE
                        tvChuaKhaiBao.text = "Không thể tải lịch sử khai báo"
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("KhaiBao", "Lỗi kết nối: ${e.message}")
                    tvChuaKhaiBao.visibility = View.VISIBLE
                    tvChuaKhaiBao.text = "Lỗi kết nối mạng"
                }
            }
        }
    }

    // Extension function chuyển dp → px
    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}