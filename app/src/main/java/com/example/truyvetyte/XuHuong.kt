package com.example.truyvetyte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class XuHuong : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Dòng này cực kỳ quan trọng: Kết nối với layout bạn của bạn đã làm
        return inflater.inflate(R.layout.dashboard_screen, container, false)
    }
}