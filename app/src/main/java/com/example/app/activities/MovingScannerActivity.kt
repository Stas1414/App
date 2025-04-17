package com.example.app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.app.R
import com.example.app.model.Document
import com.example.app.service.DocumentService
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class MovingScannerActivity : AppCompatActivity() {

    private enum class ScanMode { NONE, DOCUMENT, PRODUCT }
    private var currentScanMode = ScanMode.NONE


    private lateinit var previewView: PreviewView
    private lateinit var textViewStatus: TextView
    private lateinit var layoutScanChoice: LinearLayout
    private lateinit var btnScanDocument: Button
    private lateinit var btnScanProduct: Button


    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private var cameraProvider: ProcessCameraProvider? = null
    private val isProcessingBarcode = AtomicBoolean(false)


    private val documentService = DocumentService()



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("PERMISSION", "Camera permission granted")

                startCameraPreview()

            } else {
                Log.e("PERMISSION", "Camera permission denied")
                showToast("Необходимо разрешение на использование камеры для сканирования")
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moving_scanner)


        previewView = findViewById(R.id.previewView)
        textViewStatus = findViewById(R.id.textViewStatus)
        layoutScanChoice = findViewById(R.id.layoutScanChoice)
        btnScanDocument = findViewById(R.id.btnScanDocument)
        btnScanProduct = findViewById(R.id.btnScanProduct)


        textViewStatus.text = "Выберите тип сканирования"
        layoutScanChoice.visibility = View.VISIBLE


        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()


        setupChoiceButtons()


        checkCameraPermissionAndStartPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
        cameraProvider?.unbindAll()
        Log.d("ScannerActivity", "Resources released")
    }

    private fun setupChoiceButtons() {
        btnScanDocument.setOnClickListener {
            selectScanMode(ScanMode.DOCUMENT)
        }
        btnScanProduct.setOnClickListener {
            selectScanMode(ScanMode.PRODUCT)
        }
    }

    private fun selectScanMode(mode: ScanMode) {
        currentScanMode = mode
        layoutScanChoice.visibility = View.GONE
        val statusText = when (mode) {
            ScanMode.DOCUMENT -> "Наведите на штрихкод документа"
            ScanMode.PRODUCT -> "Наведите на штрихкод товара"
            ScanMode.NONE -> "Ошибка выбора режима"
        }
        textViewStatus.text = statusText
        Log.i("SCAN_MODE", "Scan mode selected: $mode")
        startImageAnalysis()
    }

    private fun checkCameraPermissionAndStartPreview() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PERMISSION", "Camera permission already available")
                startCameraPreview()
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


    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, preview)
                Log.i("CAMERA", "Camera Preview bound successfully")
            } catch (exc: Exception) {
                Log.e("CAMERA", "Preview binding failed", exc)
                showToast("Не удалось запустить превью камеры: ${exc.message}")
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun startImageAnalysis() {
        val cameraProvider = this.cameraProvider ?: run {
            Log.e("CAMERA", "CameraProvider not available to start analysis")
            return
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                    if (currentScanMode != ScanMode.NONE && !isProcessingBarcode.get()) {
                        processImage(imageProxy)
                    } else {
                        imageProxy.close()
                    }
                }
            }

        val newPreview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                newPreview,
                imageAnalyzer
            )
            Log.i("CAMERA", "Camera Preview and ImageAnalysis bound successfully")
        } catch (exc: Exception) {
            Log.e("CAMERA", "Use case binding with analysis failed", exc)
            showToast("Не удалось запустить анализ штрихкодов: ${exc.message}")
            resetToChoiceState("Ошибка запуска анализатора")
        }
    }

    @SuppressLint("UnsafeOptInUsageError", "SetTextI18n")
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcodeValue = barcodes.firstNotNullOfOrNull { it.rawValue }

                        if (barcodeValue != null && isProcessingBarcode.compareAndSet(false, true)) {
                            Log.i("SCANNER", "Barcode found: $barcodeValue. Mode: $currentScanMode. Attempting to fetch...")
                            runOnUiThread { textViewStatus.text = "Обработка: $barcodeValue..." }

                            fetchDataAndNavigate(barcodeValue)
                        } else {
                            Log.v("SCANNER", "Already processing or no raw value in barcode.")
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


    private fun fetchDataAndNavigate(barcode: String) {
        lifecycleScope.launch {
            var errorOccurred = false
            var itemFound = false

            try {
                when (currentScanMode) {
                    ScanMode.DOCUMENT -> {
                        val document: Document? = documentService.getDocumentByBarcode(barcode)
                        if (document != null) {
                            itemFound = true
                            Log.i("API_CALL", "Document found for $barcode (ID: ${document.id}). Navigating...")
                            navigateToDocumentDetails(document)
                        }
                    }
                    ScanMode.PRODUCT -> {
                       val documents = documentService.getDocumentsByProductBarcode(barcode)
                        if (documents != null) {
                            itemFound = true
                            Log.i("API_CALL", "Documents found for $barcode. Navigating...")
                            navigateToDocumentDetailsByProduct(documents)
                        }
                    }
                    ScanMode.NONE -> {
                        Log.e("API_CALL", "fetchData called with ScanMode.NONE for barcode $barcode")
                        errorOccurred = true
                    }
                }
            } catch (e: Exception) {
                Log.e("API_CALL", "Exception during API call for $barcode (Mode: $currentScanMode)", e)
                errorOccurred = true
            }


            if (errorOccurred) {
                showToast("Ошибка сети или сервера при поиске $barcode")
                resetScannerState("Ошибка. Попробуйте снова.")
            } else if (!itemFound) {
                Log.w("API_CALL", "Item not found for barcode: $barcode (Mode: $currentScanMode)")
                handleItemNotFound(barcode)
            }
        }
    }


    private fun navigateToDocumentDetails(document: Document) {
        Log.d("NAVIGATION", "Navigating to DocumentDetailsActivity with Document ID: ${document.id}")
        runOnUiThread {
            val intent = Intent(this, DocumentMovingActivity::class.java)
            intent.putExtra("DOCUMENT_EXTRA", document)
            startActivity(intent)
            finish()
        }
    }


    private fun navigateToDocumentDetailsByProduct(documents: MutableList<Document>) {
        val documentsArrayList = ArrayList(documents)

        runOnUiThread {
            val intent = Intent(this, DocumentMovingActivity::class.java)
            intent.putExtra("PRODUCT_EXTRA", documentsArrayList)
            startActivity(intent)
            finish()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleItemNotFound(barcode: String) {
        val itemType = when (currentScanMode) {
            ScanMode.DOCUMENT -> "Документ"
            ScanMode.PRODUCT -> "Товар"
            ScanMode.NONE -> "Объект"
        }
        runOnUiThread {
            textViewStatus.text = "$itemType для штрихкода '$barcode' не найден"

            textViewStatus.postDelayed({
                resetScannerState(when (currentScanMode) {
                    ScanMode.DOCUMENT -> "Наведите на штрихкод документа"
                    ScanMode.PRODUCT -> "Наведите на штрихкод товара"
                    ScanMode.NONE -> "Выберите тип сканирования"
                })
            }, 2500)
        }
    }


    private fun resetScannerState(statusText: String) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                textViewStatus.text = statusText
                isProcessingBarcode.set(false)
                Log.d("ScannerActivity", "Scanner state reset. Ready for next scan.")
            }
        }
    }


    private fun resetToChoiceState(statusText: String? = null) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                currentScanMode = ScanMode.NONE
                layoutScanChoice.visibility = View.VISIBLE
                textViewStatus.text = statusText ?: "Выберите тип сканирования"
                isProcessingBarcode.set(false)


                cameraProvider?.let { cp ->
                    Log.d("CAMERA", "Resetting to choice state: Unbinding all and rebinding Preview only.")
                    cp.unbindAll()

                    try {

                        val newPreview = Preview.Builder()
                            .build()
                            .also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }


                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


                        cp.bindToLifecycle(
                            this,
                            cameraSelector,
                            newPreview
                        )
                        Log.i("CAMERA", "Successfully rebound Preview only after reset.")

                    } catch (e: Exception) {
                        Log.e("CAMERA", "Failed to rebind preview after choice reset", e)

                        showToast("Ошибка перезапуска камеры")
                    }
                } ?: run {
                    Log.w("CAMERA", "Cannot reset camera state, provider is null.")
                }
                Log.i("SCAN_MODE", "Scanner reset to choice mode.")
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}