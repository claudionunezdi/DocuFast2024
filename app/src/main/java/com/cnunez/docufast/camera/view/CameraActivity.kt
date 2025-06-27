package com.cnunez.docufast.camera.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.cnunez.docufast.R
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.camera.model.CameraModel
import com.cnunez.docufast.camera.presenter.CameraPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.dataclass.ImageFile
import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.camera.view.FileNameDialogFragment
import com.cnunez.docufast.camera.view.EditFileNameDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class CameraActivity : BaseActivity(), CameraContract.View {
    private lateinit var presenter: CameraContract.Presenter
    private lateinit var viewFinder: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var ocrTextView: TextView
    private lateinit var captureButton: Button
    private lateinit var applyOcrButton: Button
    private lateinit var saveTextButton: Button
    private lateinit var imageCapture: ImageCapture
    private var capturedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_camera)

        viewFinder = findViewById(R.id.viewFinder)
        capturedImageView = findViewById(R.id.capturedImageView)
        ocrTextView = findViewById(R.id.ocrResultTextView)
        captureButton = findViewById(R.id.captureButton)
        applyOcrButton = findViewById(R.id.applyOcrButton)
        saveTextButton = findViewById(R.id.saveTextButton)

        presenter = CameraPresenter(this, CameraModel(this, FirebaseDatabase.getInstance()))

        startCamera()

        captureButton.setOnClickListener {
            takePhoto()
        }

        applyOcrButton.setOnClickListener {
            capturedBitmap?.let { presenter.applyOcr(it) }
        }

        saveTextButton.setOnClickListener {
            FileNameDialogFragment("OCR.txt") { name ->
                presenter.saveOcrText(name)
            }.show(supportFragmentManager, "FileNameDialog")
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                showError("Error al iniciar cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    image.close()
                    capturedBitmap = bitmap
                    showPhoto(bitmap)
                    applyOcrButton.visibility = View.VISIBLE
                }

                override fun onError(exception: ImageCaptureException) {
                    showError("Error al capturar: ${exception.message}")
                }
            })
    }

    override fun onUserAuthenticated(user: FirebaseUser) {}

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showPhoto(bitmap: Bitmap) {
        viewFinder.visibility = View.GONE
        capturedImageView.visibility = View.VISIBLE
        capturedImageView.setImageBitmap(bitmap)
    }

    override fun showOcrResult(text: String) {
        ocrTextView.text = text
        saveTextButton.visibility = View.VISIBLE
    }

    override fun showFileSaved(textFile: TextFile) {
        Toast.makeText(this, "Archivo guardado: ${textFile.fileName}", Toast.LENGTH_SHORT).show()
    }

    override fun showImageSaved(imageFile: ImageFile) {
        Toast.makeText(this, "Imagen guardada: ${imageFile.id}", Toast.LENGTH_SHORT).show()
    }

    override fun showEditFileNameDialog(fileId: String, callback: (String) -> Unit) {
        EditFileNameDialogFragment(fileId, callback)
            .show(supportFragmentManager, "EditFileNameDialog")
    }
}
