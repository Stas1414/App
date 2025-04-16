package com.example.app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.app.R
import com.example.app.model.Deal
import com.example.app.service.DealService

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeScannerActivity : AppCompatActivity() {


    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private val dealService = DealService()


    private lateinit var previewView: PreviewView
    private lateinit var textViewStatus: TextView


    private var cameraProvider: ProcessCameraProvider? = null


    private val isProcessingBarcode = AtomicBoolean(false)


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("PERMISSION", "Camera permission granted")
                startCamera()
            } else {
                Log.e("PERMISSION", "Camera permission denied")
                Toast.makeText(this, "Необходимо разрешение на использование камеры для сканирования", Toast.LENGTH_LONG).show()
                finish()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)


        previewView = findViewById(R.id.previewView)
        textViewStatus = findViewById(R.id.textViewResult)
        textViewStatus.text = "Наведите камеру на штрихкод"


        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()


        checkCameraPermissionAndStart()
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraExecutor.shutdown()
        barcodeScanner.close()
        cameraProvider?.unbindAll()
        Log.d("BarcodeScannerActivity", "Resources released")
    }


    private fun checkCameraPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PERMISSION", "Camera permission already available")
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {

                Log.w("PERMISSION", "Showing rationale for camera permission is recommended")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {

                Log.d("PERMISSION", "Requesting camera permission")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {

                cameraProvider = cameraProviderFuture.get()


                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }


                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                            if (!isProcessingBarcode.get()) {
                                processImage(imageProxy)
                            } else {

                                imageProxy.close()
                            }
                        }
                    }


                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


                cameraProvider?.unbindAll()


                cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                Log.i("CAMERA", "Camera Use Cases bound successfully")

            } catch (exc: Exception) {
                Log.e("CAMERA", "Use case binding failed", exc)
                Toast.makeText(this, "Не удалось запустить камеру: ${exc.message}", Toast.LENGTH_LONG).show()
                finish() // Закрываем, если не удалось запустить камеру
            }
        }, ContextCompat.getMainExecutor(this)) // Listener выполняется в главном потоке
    }


    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)


            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->

                    if (barcodes.isNotEmpty()) {

                        val barcodeValue = barcodes.firstNotNullOfOrNull { it.rawValue }


                        if (barcodeValue != null && isProcessingBarcode.compareAndSet(false, true)) {
                            Log.i("SCANNER", "Barcode found: $barcodeValue. Attempting to fetch deal...")
                            runOnUiThread { textViewStatus.text = "Поиск сделки: $barcodeValue..." }


                            fetchDealAndNavigate(barcodeValue)
                        } else {

                            Log.v("SCANNER", "Already processing or no raw value in detected barcodes.")
                        }
                    } else {

                        Log.v("SCANNER", "No barcodes detected in this frame.")
                    }
                }
                .addOnFailureListener { e ->

                    Log.e("SCANNER", "ML Kit barcode scanning failed", e)
                    isProcessingBarcode.set(false)
                    runOnUiThread { textViewStatus.text = "Ошибка сканера. Попробуйте снова." }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }


    private fun fetchDealAndNavigate(barcode: String) {
        lifecycleScope.launch {
            var deal: Deal? = null
            var errorOccurred = false
            try {


                deal = dealService.getDealByBarcode(barcode)

            } catch (e: Exception) {

                Log.e("API_CALL", "Exception during getDealByBarcode call for $barcode", e)
                errorOccurred = true
            }


            if (errorOccurred) {
                runOnUiThread {
                    Toast.makeText(this@BarcodeScannerActivity, "Ошибка сети или сервера", Toast.LENGTH_SHORT).show()
                    textViewStatus.text = "Ошибка. Попробуйте снова."
                }
                isProcessingBarcode.set(false)
            } else if (deal != null) {

                Log.i("API_CALL", "Deal found for $barcode (ID: ${deal.id}). Navigating...")
                navigateToDealDetails(deal)

            } else {
                Log.w("API_CALL", "Deal not found for barcode: $barcode. Service returned null.")
                handleBarcodeNotFound(barcode)
            }
        }
    }


    private fun navigateToDealDetails(deal: Deal) {
        Log.d("NAVIGATION", "Navigating to DealActivity with Deal ID: ${deal.id}")

        val intent = Intent(this, DealActivity::class.java)
        intent.putExtra("DEAL_EXTRA", deal)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun handleBarcodeNotFound(barcode: String) {
        runOnUiThread {
            textViewStatus.text = "Сделка для штрихкода '$barcode' не найдена"

            textViewStatus.postDelayed({
                if (!isProcessingBarcode.get()) {
                    textViewStatus.text = "Наведите камеру на штрихкод"
                }
                isProcessingBarcode.set(false)
            }, 2000)
        }
    }
}