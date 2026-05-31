package com.example.truyvetyte

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView // Thêm import TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.network.RetrofitClient
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import android.content.Context
import java.text.NumberFormat
import java.util.Locale
class XuHuong : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var tvDose1: TextView
    private lateinit var tvDose2: TextView
    private lateinit var tvStatusMain: TextView
    private lateinit var tvStatusDesc: TextView
    private lateinit var tvStatF0: TextView
    private lateinit var tvStatF1F2: TextView
    private lateinit var tvStatQuarantine: TextView
    private lateinit var tvStatRedZone: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dashboard_screen, container, false)

        // Ánh xạ layout
        barChart = view.findViewById(R.id.barChart)
        tvDose1 = view.findViewById(R.id.tv_vaccine_dose_1)
        tvDose2 = view.findViewById(R.id.tv_vaccine_dose_2)

        // Ánh xạ View trạng thái
        tvStatusMain = view.findViewById(R.id.tv_status_main)
        tvStatusDesc = view.findViewById(R.id.tv_status_desc)
        tvStatF0 = view.findViewById(R.id.tv_stat_f0)
        tvStatF1F2 = view.findViewById(R.id.tv_stat_f1_f2)
        tvStatQuarantine = view.findViewById(R.id.tv_stat_quarantine)
        tvStatRedZone = view.findViewById(R.id.tv_stat_red_zone)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchTrendData()
        fetchVaccineRates()
        fetchDashboardSummary()
    }
    override fun onResume() {
        super.onResume()
        updateHealthStatus()
    }
    private fun fetchDashboardSummary() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    val summaryResponse = response.body()!!

                    if (summaryResponse.success && summaryResponse.data != null) {
                        // Format số liệu thành dạng có dấu chấm (VD: 17.000)
                        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

                        tvStatF0.text = formatter.format(summaryResponse.data.SoCaF0)
                        tvStatF1F2.text = formatter.format(summaryResponse.data.SoCaF1F2)
                        tvStatQuarantine.text = formatter.format(summaryResponse.data.SoNguoiCachLy)
                        tvStatRedZone.text = formatter.format(summaryResponse.data.SoVungNguyHiem)
                    } else {
                        Log.e("XuHuongFragment", summaryResponse.message)
                    }
                } else {
                    Log.e("XuHuongFragment", "Lỗi lấy dữ liệu tổng quan từ server")
                }
            } catch (e: Exception) {
                Log.e("XuHuongFragment", "Lỗi kết nối dashboard summary: ${e.message}")
            }
        }
    }

    private fun updateHealthStatus() {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        // Lấy cờ IS_POSITIVE ra, nếu không có thì mặc định là false (An toàn)
        val isPositive = sharedPreferences.getBoolean("IS_POSITIVE", false)

        if (isPositive) {
            // Đổi giao diện báo động đỏ
            tvStatusMain.text = "Nguy Hiểm"
            tvStatusMain.setTextColor(Color.parseColor("#EF5350")) // Màu đỏ cảnh báo

            tvStatusDesc.text = "Cảnh báo: Phát hiện yếu tố dịch tễ Dương Tính. Vui lòng cách ly ngay lập tức và liên hệ với cơ sở y tế gần nhất hoặc gọi đường dây nóng của Bộ Y tế!"
            tvStatusDesc.setTextColor(Color.parseColor("#EF5350"))
        } else {
            // Trả về giao diện an toàn ban đầu
            tvStatusMain.text = "An Toàn"
            tvStatusMain.setTextColor(Color.parseColor("#42A5F5")) // Màu xanh

            tvStatusDesc.text = "Trạng thái dịch tễ của bạn hiện tại không có nguy cơ lây nhiễm cao. Tiếp tục tuân thủ quy tắc 5K."
            tvStatusDesc.setTextColor(Color.parseColor("#5C94F0"))
        }
    }

    private fun fetchVaccineRates() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getVaccineRates()
                if (response.isSuccessful && response.body() != null) {
                    val rateResponse = response.body()!!

                    if (rateResponse.success && rateResponse.data != null) {
                        // Nối thêm ký tự % vào text
                        tvDose1.text = "${rateResponse.data.tyLeMui1}%"
                        tvDose2.text = "${rateResponse.data.tyLeMui2}%"
                    }
                } else {
                    Log.e("XuHuongFragment", "Lỗi lấy dữ liệu tiêm chủng từ server")
                }
            } catch (e: Exception) {
                Log.e("XuHuongFragment", "Lỗi kết nối tiêm chủng: ${e.message}")
            }
        }
    }

    private fun fetchTrendData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTrendAnalysis()
                if (response.isSuccessful && response.body() != null) {
                    val trendResponse = response.body()!!

                    if (trendResponse.success) {
                        setupBarChart(trendResponse.data)
                    } else {
                        Toast.makeText(requireContext(), trendResponse.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi từ server", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("XuHuongFragment", "Lỗi kết nối: ${e.message}")
                Toast.makeText(requireContext(), "Không thể kết nối tới server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBarChart(monthlyData: List<Int>) {
        val entries = ArrayList<BarEntry>()

        for (i in 0 until 12) {
            val f0Count = if (i < monthlyData.size) monthlyData[i].toFloat() else 0f
            entries.add(BarEntry(i.toFloat(), f0Count))
        }

        val dataSet = BarDataSet(entries, "Số ca nhiễm (F0)")
        dataSet.color = Color.parseColor("#5C94F0")
        dataSet.valueTextColor = Color.parseColor("#5C94F0")
        dataSet.valueTextSize = 10f

        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value == 0f) "" else value.toInt().toString()
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        barChart.data = barData

        val months = arrayOf("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(months)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = Color.parseColor("#4DB6B0")

        barChart.axisRight.isEnabled = false

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.axisMinimum = 0f
        leftAxis.textColor = Color.parseColor("#90CAF9")
        leftAxis.spaceTop = 15f

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)

        barChart.invalidate()
    }
}