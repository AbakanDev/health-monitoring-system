package com.example.truyvetyte

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class XuHuong : Fragment() {

    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ánh xạ layout
        val view = inflater.inflate(R.layout.dashboard_screen, container, false)

        // Tìm biểu đồ thông qua view
        barChart = view.findViewById(R.id.barChart)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gọi API ngay khi view được tạo xong
        fetchTrendData()
    }

    private fun fetchTrendData() {
        // Trong Fragment, sử dụng viewLifecycleOwner.lifecycleScope thay vì lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTrendAnalysis()
                if (response.isSuccessful && response.body() != null) {
                    val trendResponse = response.body()!!

                    if (trendResponse.success) {
                        // Nạp dữ liệu 12 tháng từ API vào biểu đồ
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

        // Chạy vòng lặp 12 tháng (từ 0 đến 11)
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
                // Ẩn số 0 để biểu đồ đỡ rối
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

        // Cập nhật lại giao diện
        barChart.invalidate()
    }
}