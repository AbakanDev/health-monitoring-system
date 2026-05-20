package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment

class KhaiBao : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Dòng này cực kỳ quan trọng: Kết nối với layout bạn của bạn đã làm
        return inflater.inflate(R.layout.initial_health_declaration_screen, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tìm nút bấm trong layout của Fragment (Trang 1)
        // Giả sử nút đó có ID là btnHoanTat
        val btnGo = view.findViewById<Button>(R.id.btnHoanTat1)

        // 2. Thiết lập click
        btnGo.setOnClickListener {
            // Sử dụng Intent để mở Activity mới (Trang 2)
            // Lưu ý: Dùng requireContext() thay vì 'this'
            val intent = Intent(requireContext(), ChucMungKhaiBao::class.java)
            startActivity(intent)

        }
    }
}

