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
import java.util.concurrent.atomic.AtomicBoolean
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.truyvetyte.network.RetrofitClient

class ScannerFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    // ── AtomicBoolean: thread-safe hơn @Volatile cho use case này ─────────────
    private val isScanning = AtomicBoolean(true)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else Toast.makeText(requireContext(), "Bạn cần cấp quyền Camera để quét mã", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.qr_scanner_screen, container, false)
        previewView = view.findViewById(R.id.previewView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) startCamera()
        else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                .also { it.setAnalyzer(cameraExecutor) { proxy -> processImageProxy(proxy) } }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("ScannerFragment", "Lỗi khởi động camera", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // compareAndSet(true, false): chỉ 1 thread duy nhất vượt qua được
        if (!isScanning.compareAndSet(true, false)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            isScanning.set(true) // trả lại flag nếu không có ảnh
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        BarcodeScanning.getClient().process(image)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstOrNull()?.rawValue
                if (rawValue != null) {
                    Log.d("ScannerFragment", "QR đọc được: $rawValue") // debug giá trị QR
                    sendCheckInData(rawValue)
                } else {
                    isScanning.set(true) // không tìm thấy mã → cho quét tiếp
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScannerFragment", "Lỗi đọc barcode", e)
                isScanning.set(true)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun sendCheckInData(maKhuVuc: String) {
        val sharedPref = requireActivity().getSharedPreferences("AppPrefs", 0)
        val token = sharedPref.getString("TOKEN", "") ?: ""

        Log.d("ScannerFragment", "Token gửi lên: '$token'") // debug token có rỗng không

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.sendCheckIn(
                    token = "Bearer $token",
                    body  = mapOf("MaKhuVuc" to maKhuVuc)
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        Toast.makeText(
                            requireContext(),
                            "✅ ${data?.tenKhuVuc ?: maKhuVuc}\n${data?.trangThaiKhuVuc} • ${data?.thoiGianCheckIn}",
                            Toast.LENGTH_LONG
                        ).show()
                        // isScanning giữ false → không quét lại sau khi thành công
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ScannerFragment", "Lỗi ${response.code()}: $errorBody")
                        Toast.makeText(requireContext(), "❌ Lỗi ${response.code()}", Toast.LENGTH_SHORT).show()
                        isScanning.set(true) // lỗi → cho phép quét lại
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ScannerFragment", "Lỗi kết nối", e)
                    Toast.makeText(requireContext(), "💥 ${e.message}", Toast.LENGTH_SHORT).show()
                    isScanning.set(true)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}