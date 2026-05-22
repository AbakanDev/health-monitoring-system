package com.example.truyvetyte

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.truyvetyte.network.RetrofitClient

class ScannerFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    // Bước 1: Xử lý xin quyền Camera một cách hiện đại trong Fragment
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Bạn cần cấp quyền Camera để quét mã", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Nạp layout quét mã mà bạn đã có sẵn
        val view = inflater.inflate(R.layout.qr_scanner_screen, container, false)
        previewView = view.findViewById(R.id.previewView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Kiểm tra quyền ngay khi vào trang
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                // QUAN TRỌNG: Dùng viewLifecycleOwner thay vì 'this'
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            // NGĂN CHẶN QUÉT LIÊN TỤC: Khi đã quét được 1 mã, tạm dừng xử lý tiếp
                            // Bạn có thể dùng một biến flag 'isScanning = false' ở đây

                            sendCheckInData(rawValue) // Gọi hàm gửi dữ liệu
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun sendCheckInData(maKhuVuc: String) {
        // 1. AI: Lấy MaNguoiDung (Giả sử bạn đã lưu vào SharedPreferences khi đăng nhập)
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", 0)
        val maNguoiDung = sharedPref.getString("USER_ID", "anonymous") ?: "anonymous"

        // 2. LÚC NÀO: Lấy thời gian hiện tại
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val thoiGian = formatter.format(Date())

        // 3. ĐÓNG GÓI DỮ LIỆU
        val request = CheckInRequest(maNguoiDung, maKhuVuc, thoiGian)

        // 4. GỬI LÊN SERVER
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Lưu ý: Đảm bảo RetrofitClient của bạn đã có hàm sendCheckIn
                val response = RetrofitClient.instance.sendCheckIn(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "✅ Check-in thành công tại: $maKhuVuc", Toast.LENGTH_LONG).show()
                        // Có thể chuyển người dùng sang trang Lịch Sử sau khi thành công
                    } else {
                        Toast.makeText(requireContext(), "❌ Lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                // ... thành công ...
                            } else {
                                // In ra mã lỗi và nội dung lỗi từ Server
                                val errorBody = response.errorBody()?.string()
                                Log.e("ScannerError", "Mã lỗi: ${response.code()} - Nội dung: $errorBody")
                                Toast.makeText(requireContext(), "Lỗi: ${response.code()} - Kiểm tra Logcat", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "💥 Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

}