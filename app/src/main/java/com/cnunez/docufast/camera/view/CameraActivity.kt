package com.cnunez.docufast.camera.view

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cnunez.docufast.R
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.camera.model.CameraModel
import com.cnunez.docufast.camera.presenter.CameraPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity(), CameraContract.View {

    // Views
    private lateinit var viewFinder: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var ocrTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var applyOcrButton: Button
    private lateinit var saveTextButton: Button

    // Camera
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private var capturedBitmap: Bitmap? = null

    // Presenter
    private lateinit var presenter: CameraPresenter

    // Context
    private lateinit var currentGroupId: String
    private lateinit var currentOrganizationId: String

    private var lastOcrResult: String? = null

    // Permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            showError("Camera permission required")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_camera)

        // Get group and organization context
        currentGroupId = intent.getStringExtra("groupId") ?: run {
            showError("Group context missing")
            finish()
            return
        }
        currentOrganizationId = intent.getStringExtra("organizationId") ?: run {
            showError("Organization context missing")
            finish()
            return
        }

        initViews()
        setupPresenter()
        checkCameraPermission()
    }

    private fun initViews() {
        viewFinder = findViewById(R.id.viewFinder)
        capturedImageView = findViewById(R.id.capturedImageView)
        ocrTextView = findViewById(R.id.ocrResultTextView)
        captureButton = findViewById(R.id.captureButton)
        applyOcrButton = findViewById(R.id.applyOcrButton)
        saveTextButton = findViewById(R.id.saveTextButton)

        // Setup buttons
        captureButton.setOnClickListener { takePhoto() }
        applyOcrButton.setOnClickListener { capturedBitmap?.let { presenter.applyOcr(it) } }
        saveTextButton.setOnClickListener { showSaveDialog() }

        // Initially hide OCR-related buttons
        applyOcrButton.visibility = View.GONE
        saveTextButton.visibility = View.GONE
    }

    private fun setupPresenter() {
        presenter = CameraPresenter(
            this,
            CameraModel(this, FirebaseDatabase.getInstance())
        ).apply {
            setGroupContext(currentGroupId, currentOrganizationId)
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

            } catch(exc: Exception) {
                showError("Failed to start camera: ${exc.message}")
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun takePhoto() {
        val photoFile = createImageFile() ?: run {
            showError("No se pudo crear el archivo para la foto")
            return
        }

        // Opción 1: Usando File directamente (recomendado)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Opción 2: Si necesitas usar Uri (para versiones específicas)
        // val outputOptions = ImageCapture.OutputFileOptions.Builder(
        //     contentResolver,
        //     MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        //     createContentValues("IMG_${System.currentTimeMillis()}")
        // ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    processCapturedImage(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    showError("Error al capturar foto: ${exc.message}")
                }
            }
        )
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply { createNewFile() }
        } catch (ex: IOException) {
            Log.e("Camera", "Error creating file", ex)
            null
        }
    }

    private fun processCapturedImage(imageFile: File) {
        try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            capturedBitmap = bitmap
            showPhoto(bitmap)
            applyOcrButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            showError("Error al procesar imagen: ${e.message}")
        }
    }

    private fun createContentValues(displayName: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
    }


    private fun processCapturedImage(uri: Uri?) {
        uri ?: run {
            showError("Invalid image URI")
            return
        }

        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                capturedBitmap = bitmap
                showPhoto(bitmap)
                applyOcrButton.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            showError("Failed to process image: ${e.message}")
        }
    }

    private fun createImageFileUri(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val tempFile = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply { createNewFile() }

            FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                tempFile
            )
        } catch (ex: Exception) {
            Log.e("Camera", "Error creating temp file", ex)
            null
        }
    }

    private fun showSaveDialog() {
        val defaultName = "DOC_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.txt"

        val dialogView = layoutInflater.inflate(R.layout.dialog_file_name, null)
        val input = dialogView.findViewById<EditText>(R.id.etFileName)
        input.setText(defaultName)

        AlertDialog.Builder(this)
            .setTitle("Save OCR Result")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val fileName = input.text.toString().takeIf { it.isNotBlank() } ?: defaultName
                presenter.saveOcrText(fileName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showPhoto(bitmap: Bitmap) {
        runOnUiThread {
            viewFinder.visibility = View.GONE
            capturedImageView.visibility = View.VISIBLE
            capturedImageView.setImageBitmap(bitmap)
        }
    }

    override fun showOcrResult(text: String) {
        runOnUiThread {
            lastOcrResult = text
            ocrTextView.text = text
            saveTextButton.visibility = View.VISIBLE
        }
    }

    override fun showFileSaved(textFile: TextFile) {
        runOnUiThread {
            Toast.makeText(
                this,
                "File saved: ${textFile.fileName}",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun showImageSaved(imageFile: ImageFile) {
        runOnUiThread {
            Toast.makeText(
                this,
                "Image saved: ${imageFile.id}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun isOcrResultAvailable(): Boolean {
        return !lastOcrResult.isNullOrEmpty()
    }

    override fun getOcrResult(): String? {
        return lastOcrResult
    }

    override fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.e("CameraActivity", message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}