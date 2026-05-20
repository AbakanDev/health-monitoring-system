package com.example.truyvetyte

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.lifecycleScope
import com.example.truyvetyte.model.RegisterRequest
import com.example.truyvetyte.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class PersonalDetailActivity : AppCompatActivity() {

    // 2 biến này sẽ hứng dữ liệu từ màn hình trước truyền sang
    private var passedCccd = ""
    private var passedPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nhớ đảm bảo tên layout đúng với file XML số 2 của bạn
        setContentView(R.layout.personal_detail_screen)

        // Nhận dữ liệu từ RegisterActivity gửi sang
        passedCccd = intent.getStringExtra("EXTRA_CCCD") ?: ""
        passedPassword = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

        val edtFullName = findViewById<AppCompatEditText>(R.id.edtFullName)
        val edtDob = findViewById<AppCompatEditText>(R.id.edtDob)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val edtAddress = findViewById<AppCompatEditText>(R.id.edtAddress)
        val edtEmail = findViewById<AppCompatEditText>(R.id.edtEmail)
        val edtPhone = findViewById<AppCompatEditText>(R.id.edtPhone)
        val btnSubmit = findViewById<ImageView>(R.id.btnSubmit)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Setup DatePicker cho ô ngày sinh
        edtDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedMonth = String.format("%02d", selectedMonth + 1)
                    val formattedDay = String.format("%02d", selectedDay)
                    val dateString = "$selectedYear-$formattedMonth-$formattedDay"
                    edtDob.setText(dateString)
                },
                year, month, day
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val dob = edtDob.text.toString().trim()
            val address = edtAddress.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val phone = edtPhone.text.toString().trim()

            // Lấy giới tính từ RadioGroup
            val selectedGenderId = rgGender.checkedRadioButtonId
            val gender = if (selectedGenderId == R.id.rbMale) "Nam" else if (selectedGenderId == R.id.rbFemale) "Nữ" else ""

            if (fullName.isEmpty() || dob.isEmpty() || gender.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gộp tất cả data vào Model để gửi lên API
            val requestData = RegisterRequest(
                cccd = passedCccd,
                password = passedPassword,
                fullName = fullName,
                dob = dob,
                gender = gender,
                address = address,
                email = email,
                phone = phone
            )

            submitDataToBackend(requestData)
        }
    }

    private fun submitDataToBackend(requestData: RegisterRequest) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.registerUser(requestData)

                withContext(Dispatchers.Main) {
                    // Xóa điều kiện "==" gây lỗi, chỉ cần check HTTP Code 200-299 là đủ
                    if (response.isSuccessful) {
                        // Chuyển sang màn hình thành công
                        val intent = Intent(this@PersonalDetailActivity, RegisterSuccessActivity::class.java)
                        // Xóa các trang trước đó khỏi stack để người dùng không back lại form đăng ký được
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Xử lý lỗi trả về từ Server
                        var errorMessage = "Lỗi hệ thống hoặc trùng dữ liệu"
                        try {
                            val errorString = response.errorBody()?.string()
                            if (errorString != null) {
                                val jsonObject = org.json.JSONObject(errorString)
                                errorMessage = jsonObject.optString("message", errorMessage)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Toast.makeText(this@PersonalDetailActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PersonalDetailActivity, "Lỗi kết nối: Không thể gọi Server", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}