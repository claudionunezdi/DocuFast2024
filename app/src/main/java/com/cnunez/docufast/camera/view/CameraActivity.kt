package com.cnunez.docufast.camera.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.cnunez.docufast.R
import com.cnunez.docufast.camera.contract.CameraContract
import com.cnunez.docufast.camera.data.FileRepository
import com.cnunez.docufast.camera.model.CameraModel
import com.cnunez.docufast.camera.presenter.CameraPresenter
import com.cnunez.docufast.common.base.BaseActivity
import com.cnunez.docufast.common.firebase.AppDatabase
import com.cnunez.docufast.common.firebase.AppDatabase.fileDao
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.storage.FileStorageManager
import com.cnunez.docufast.common.firebase.storage.FirebaseStorageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity(), CameraContract.View {

    // Views
    private lateinit var viewFinder: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var saveButton: Button
    private lateinit var ocrButton: Button
    private lateinit var flashButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var ocrResultTextView: TextView
    private lateinit var ocrResultContainer: ScrollView

    // Camera components
    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null
    private var currentBitmap: Bitmap? = null
    private var isFlashOn = false

    // Presenter and context data
    private lateinit var presenter: CameraPresenter
    private lateinit var currentGroupId: String
    private lateinit var currentOrganizationId: String
    private val currentUserId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // Firebase components
    //private val database = FirebaseDatabase.getInstance()
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val storageManager = FileStorageManager()
    private val storage = FirebaseStorage.getInstance()
    private val fileDao by lazy { FileDaoRealtime(database, FileStorageManager()) }
    private val userDao by lazy { UserDaoRealtime(database) }
    private val fileRepository by lazy { FileRepository() }


    // Permissions
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            plus(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.all { it.value } -> startCamera()
            requiredPermissions.any { !shouldShowRequestPermissionRationale(it) } -> showPermissionSettingsDialog()
            else -> showError("Se requieren todos los permisos")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_camera)


        if (currentUserId.isEmpty()) {
            showErrorAndFinish("Usuario no autenticado")
            return
        }

        // Inicialización única de vistas
        initViews()

        // Verificación de cámara
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            showErrorAndFinish("Este dispositivo no tiene cámara")
            return
        }

        // Obtener IDs del intent (sin shadowing)
        currentGroupId = intent.getStringExtra("groupId") ?: run {
            showErrorAndFinish("Falta ID de grupo")
            return
        }
        currentOrganizationId = intent.getStringExtra("organizationId") ?: run {
            showErrorAndFinish("Falta ID de organización")
            return
        }

        // Inicializar presenter
        presenter = CameraPresenter(
            view = this,
            model = CameraModel(
                applicationContext,
                fileDao,          // FileDaoRealtime
                userDao,          // UserDaoRealtime
                fileRepository    // FileRepository
            )
        ).apply {
            setGroupContext(
                groupId = currentGroupId,
                organizationId = currentOrganizationId,
                userId = currentUserId,
                metadata = mapOf(
                    "source" to "camera_activity",
                    "deviceModel" to Build.MODEL,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }

        // Verificar permisos e iniciar cámara
        checkCameraPermissions()


        // Initialize FileDaoRealtime with dependencies
        presenter = CameraPresenter(
            view = this,
            model = CameraModel(
                applicationContext, fileDao,
                userDao = userDao,
                repository = fileRepository
            )
        ).apply {
            setGroupContext(
                groupId = currentGroupId,
                organizationId = currentOrganizationId,
                userId = currentUserId,
                metadata = mapOf( // Ahora sí reconocerá el parámetro
                    "source" to "camera_activity",
                    "deviceModel" to Build.MODEL,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }


        checkCameraPermissions()
    }

    private fun initViews() {
        try {
            captureButton = findViewById(R.id.captureButton)
            // Inicializa TODAS las demás vistas aquí
            saveButton = findViewById(R.id.saveButton)
            viewFinder = findViewById(R.id.viewFinder)
            capturedImageView = findViewById(R.id.capturedImageView)
            captureButton = findViewById(R.id.captureButton) // Asegúrate que este ID existe
            saveButton = findViewById(R.id.saveButton)
            ocrButton = findViewById(R.id.applyOcrButton)
            flashButton = findViewById(R.id.flashButton)
            progressBar = findViewById(R.id.progressBar)
            ocrResultTextView = findViewById(R.id.ocrResultTextView)
            ocrResultContainer = findViewById(R.id.ocrResultContainer)

            captureButton.setOnClickListener { takePhoto() }
            saveButton.setOnClickListener { showSaveOptionsDialog() }
            ocrButton.setOnClickListener { processOcr() }
            flashButton.setOnClickListener { toggleFlash() }

            saveButton.isEnabled = false
            ocrButton.isEnabled = false
            ocrResultContainer.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("CameraActivity", "Error inicializando vistas", e)
            showErrorAndFinish("Error inicializando la cámara")
        }
    }

    private fun checkCameraPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            permissionsLauncher.launch(requiredPermissions)
        }
    }


    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(viewFinder.surfaceProvider) }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                    .build()

                // Asegurarse de que la vista esté visible
                viewFinder.visibility = View.VISIBLE
                capturedImageView.visibility = View.GONE

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

            } catch (e: Exception) {
                showErrorAndFinish("Error al configurar cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        flashButton.text = if (isFlashOn) "Flash ON" else "Flash OFF"
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            showError("Cámara no lista")
            return
        }

        val photoFile = try {
            createImageFile() ?: throw IOException("No se pudo crear archivo temporal")
        } catch (e: IOException) {
            showError("Error al crear archivo: ${e.message}")
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    MainScope().launch {
                        try {
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            bitmap?.let {
                                currentBitmap = it
                                displayCapturedImage(it)
                                enableSaveOptions()
                                ocrResultContainer.visibility = View.GONE
                            } ?: showError("Imagen no válida")
                        } catch (e: Exception) {
                            showError("Error al procesar imagen: ${e.message}")
                        } finally {
                            photoFile.delete()
                        }
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    showError("Error al capturar imagen: ${exc.message}")
                    if (exc.imageCaptureError == ImageCapture.ERROR_CAMERA_CLOSED) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            startCamera()
                        }, 1000)
                    }
                }
            }
        )
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            File(externalCacheDir ?: cacheDir, "JPEG_${timeStamp}_${UUID.randomUUID()}.jpg").apply {
                createNewFile()
            }
        } catch (e: IOException) {
            Log.e("CameraActivity", "Error al crear archivo", e)
            null
        }
    }

    private fun processOcr() {
        val bitmap = currentBitmap ?: run {
            showError("No hay imagen capturada")
            return
        }

        MainScope().launch {
            showLoading(true)
            try {
                presenter.applyOcr(bitmap)
            } catch (e: Exception) {
                showError("Error en OCR: ${e.localizedMessage}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showSaveOptionsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Guardar documento")
            .setItems(arrayOf("Solo imagen", "Imagen con texto OCR")) { _, which ->
                when (which) {
                    0 -> saveContent(CameraContract.SaveType.IMAGE_ONLY)
                    1 -> saveContent(CameraContract.SaveType.OCR_RESULT)
                }
            }
            .show()
    }

    private fun saveContent(saveType: CameraContract.SaveType) {
        val fileName = "doc_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"

        MainScope().launch {
            showLoading(true)
            try {
                // Asegurarse de que los IDs estén configurados
                val metadata = mapOf(
                    "groupId" to currentGroupId,
                    "organizationId" to currentOrganizationId,
                    "userId" to currentUserId,
                    "source" to "camera_activity"
                )

                // Pasar los metadatos al presenter
                val result = presenter.saveContent(fileName, saveType)

                if (result.isSuccess) {
                    showSuccess("Documento guardado exitosamente en el grupo")
                } else {
                    showError("Error al guardar: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError("Error al guardar: ${e.localizedMessage}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permisos requeridos")
            .setMessage("Activa los permisos manualmente en Ajustes")
            .setPositiveButton("Ir a ajustes") { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .setNegativeButton("Cancelar") { _, _ -> finish() }
            .show()
    }

    // Implementación de CameraContract.View
    override fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorAndFinish(message: String) {
        showError(message)
        finish()
    }

    private fun displayCapturedImage(bitmap: Bitmap?) {
        bitmap ?: return
        runOnUiThread {
            capturedImageView.setImageBitmap(bitmap)
            capturedImageView.visibility = View.VISIBLE
            viewFinder.visibility = View.GONE
        }
    }

    override fun showOcrResult(text: String) {
        runOnUiThread {
            ocrResultTextView.text = "Texto reconocido:\n$text"
            ocrResultContainer.visibility = View.VISIBLE
            saveButton.isEnabled = true
        }
    }

    override fun showSuccess(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun enableSaveOptions() {
        runOnUiThread {
            saveButton.isEnabled = true
            ocrButton.isEnabled = true
        }
    }

    override fun onFileUploaded(groupId: String) {
        val resultIntent = Intent().apply {
            putExtra("UPDATED_GROUP_ID", groupId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
        currentBitmap?.recycle()
        presenter.onDestroy()
    }
}