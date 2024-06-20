package com.cnunez.docufast.useCamera.View

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cnunez.docufast.MyApp
import com.cnunez.docufast.R
import com.cnunez.docufast.useCamera.Contract.CameraContract
import com.cnunez.docufast.useCamera.Model.Impl.CameraModelImpl
import com.cnunez.docufast.useCamera.Presenter.CameraPresenter
import com.cnunez.docufast.useCamera.Model.Photo
import com.cnunez.docufast.useCamera.Model.TextFile
import com.cnunez.docufast.useCamera.Model.PhotoDao
import com.cnunez.docufast.useCamera.Model.TextFileDao
import com.cnunez.docufast.useCamera.Model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), CameraContract.CameraView {
    private lateinit var photoUri: Uri
    private lateinit var presenter: CameraContract.CameraPresenter
    private lateinit var capturedImageView: ImageView
    private lateinit var ocrResultTextView: TextView
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoDao: PhotoDao
    private lateinit var textFileDao: TextFileDao



    companion object {
        private const val TAG = "CameraActivity"
    }

    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_camera)

        val database = (application as MyApp).database
        photoDao = database.photoDao()
        textFileDao = database.textFileDao()

        capturedImageView = findViewById(R.id.capturedImageView)
        ocrResultTextView = findViewById(R.id.ocrResultTextView)
        viewFinder = findViewById(R.id.viewFinder)
        cameraExecutor = Executors.newSingleThreadExecutor()


        // Crear una instancia de CameraModel y CameraPresenter
        val cameraModel: CameraContract.CameraModel = CameraModelImpl(this)
        presenter = CameraPresenter(this, cameraModel, this, textFileDao)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions.launch(permissions)
        }

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            presenter.onCaptureButtonClicked()
        }

        findViewById<Button>(R.id.applyOcrButton).setOnClickListener {
            presenter.onApplyOcrButtonClicked()
        }
        findViewById<Button>(R.id.saveTextButton).setOnClickListener {
            val text = ocrResultTextView.text.toString()
            if (text.isNotEmpty()) {
                val timestamp: String =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val defaultFileName = "OCR_${timestamp}.txt"
                val dialog = FileNameDialogFragment(defaultFileName) { fileName ->
                    presenter.onFileNameConfirmed(fileName, text)
                }
                dialog.show(supportFragmentManager, "FileNameDialogFragment")
            } else {
                showError("No text to save")
            }
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showPhotoTaken(photoPath: String) {
        Log.d(TAG, "Photo taken at path: $photoPath")
        try {
            photoUri = Uri.parse(photoPath)
            Log.d(TAG, "Photo URI: $photoUri")

            // Cargar la imagen en ImageView
            Glide.with(this)
                .load(photoUri)
                .into(capturedImageView)

            capturedImageView.visibility = View.VISIBLE // Show the ImageView

            findViewById<Button>(R.id.applyOcrButton).visibility = View.VISIBLE
            findViewById<Button>(R.id.saveTextButton).visibility = View.VISIBLE

            // Aplicar OCR a la imagen
            presenter.onApplyOcrButtonClicked()

            // Save photo path to database
            lifecycleScope.launch {
                val photo = Photo(uri = photoUri.toString())
                photoDao.insert(photo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Photo file not found: ${e.message}")
        }
    }
    override fun showOcrResult(text: String) {
        ocrResultTextView.text = text

        Log.d(TAG, "OCR result: $text")

        // Save OCR result to a text file and store its URI in the database
        lifecycleScope.launch {
            val textFileUri = saveTextToFile(text)
            val ocrTextFile = TextFile(uri = textFileUri.toString(), content = text, fileName = textFileUri.lastPathSegment.toString())
            textFileDao.insert(ocrTextFile)
        }
    }

    private fun saveTextToFile(text: String): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "OCR_${timeStamp}.txt"
        val storageDir: File? = getExternalFilesDir(null)
        val textFile = File(storageDir, fileName)

        try {
            FileOutputStream(textFile).use { outputStream ->
                outputStream.write(text.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showError("Error saving text file: ${e.message}")
        }

        return FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", textFile)
    }

    override fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun showImage(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .into(capturedImageView)
    }


    override fun showEditFileNameDialog(fileId: Int, callback: (String)-> Unit){
        val dialog = EditFileNameDialogFragment(fileId, callback)
        dialog.show(supportFragmentManager, "EditFileNameDialogFragment")

    }


}
