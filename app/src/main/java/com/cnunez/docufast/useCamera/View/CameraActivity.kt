package com.cnunez.docufast.useCamera.View

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.cnunez.docufast.R
import com.cnunez.docufast.useCamera.Contract.CameraContract
import com.cnunez.docufast.useCamera.Model.Impl.CameraModelImpl
import com.cnunez.docufast.useCamera.Presenter.CameraPresenter
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), CameraContract.CameraView {

    private lateinit var presenter: CameraContract.CameraPresenter
    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_camera)

        previewView = findViewById(R.id.previewView)
        val takePhotoButton: Button = findViewById(R.id.takePhotoButton)

        val cameraModel = CameraModelImpl()
        presenter = CameraPresenter(cameraModel, this)

        takePhotoButton.setOnClickListener {
            presenter.onTakePhotoClicked()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                showError("Error starting camera: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onTakePhotoClicked() {
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    presenter.onPhotoCaptured(savedUri.toString())
                }

                override fun onError(exception: ImageCaptureException) {
                    showError("Error capturing image: ${exception.message}")
                }
            }
        )
    }

    override fun showCameraPreview() {
        TODO("Not yet implemented")
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showPhotoTaken(photoPath: String) {
        Toast.makeText(this, "Photo saved to: $photoPath", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
