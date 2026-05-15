package com.xl6.opensesame

import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QrScannerActivity : ComponentActivity() {
    companion object {
        var lastScannedText: String? = null
    }

    private var locked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frame = FrameLayout(this)
        val previewView = PreviewView(this)
        frame.addView(previewView)

        frame.addView(TextView(this).apply {
            text = "Scan Autoparkki QR"
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(0x66000000)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))

        setContentView(frame)
        startCamera(previewView)
    }

    private fun startCamera(previewView: PreviewView) {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val scanner = BarcodeScanning.getClient()

            val analyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null && !locked) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            val value = barcodes.firstOrNull()?.rawValue
                            if (!value.isNullOrBlank()) {
                                locked = true
                                lastScannedText = value
                                finish()
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analyzer
            )
        }, ContextCompat.getMainExecutor(this))
    }
}
