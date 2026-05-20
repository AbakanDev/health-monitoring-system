package com.example.truyvetyte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class KhaiBao2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Nạp layout sẵn có của Trang 2
        return inflater.inflate(R.layout.initial_immigration_declaration_screen, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tìm nút bấm trong layout của Fragment (Trang 1)
        // Giả sử nút đó có ID là btnHoanTat
        val btnGo = view.findViewById<Button>(R.id.btnHoanTat2)

        // 2. Thiết lập click
        btnGo.setOnClickListener {
            // Sử dụng Intent để mở Activity mới (Trang 2)
            // Lưu ý: Dùng requireContext() thay vì 'this'
            val intent = Intent(requireContext(), ChucMungKhaiBao::class.java)
            startActivity(intent)

        }
    }
}