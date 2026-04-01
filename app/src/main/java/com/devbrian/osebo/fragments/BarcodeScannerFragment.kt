package com.devbrian.osebo.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentBarcodeScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerFragment : DialogFragment() {

    private var _binding: FragmentBarcodeScannerBinding? = null
    private val binding get() = _binding!!

    private var cameraExecutor: ExecutorService? = null
    private var onBarcodeScannedListener: ((String) -> Unit)? = null

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1001

        fun newInstance(): BarcodeScannerFragment {
            return BarcodeScannerFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_CAMERA_PERMISSION
            )
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnEnterManually.setOnClickListener {
            showManualEntryDialog()
        }

        binding.btnToggleFlash.setOnClickListener {
            toggleFlash()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Barcode scanner options
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_QR_CODE
                )
                .build()

            val scanner = BarcodeScanning.getClient(options)

            // Image analysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val rawValue = barcode.rawValue
                                if (!rawValue.isNullOrEmpty()) {
                                    imageProxy.close()
                                    onBarcodeScanned(rawValue)
                                    return@addOnSuccessListener
                                }
                            }
                        }
                        .addOnFailureListener {
                            println("❌ Barcode scanning failed: ${it.message}")
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                println("❌ Camera initialization failed: ${e.message}")
                Toast.makeText(requireContext(), "Camera failed: ${e.message}", Toast.LENGTH_SHORT).show()
                dismiss()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun toggleFlash() {
        // Flash toggle implementation
        binding.btnToggleFlash.isSelected = !binding.btnToggleFlash.isSelected
        // You'll need to get the camera and toggle flash
        Toast.makeText(requireContext(), "Flash toggled", Toast.LENGTH_SHORT).show()
    }

    private fun showManualEntryDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Enter barcode number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Enter Barcode Manually")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->
                val barcode = input.text.toString()
                if (barcode.isNotEmpty()) {
                    onBarcodeScanned(barcode)
                } else {
                    Toast.makeText(requireContext(), "Please enter a barcode", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun setOnBarcodeScannedListener(listener: (String) -> Unit) {
        this.onBarcodeScannedListener = listener
    }

    private fun onBarcodeScanned(barcode: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), "Scanned: $barcode", Toast.LENGTH_SHORT).show()
            onBarcodeScannedListener?.invoke(barcode)
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        _binding = null
    }
}