package com.cnunez.docufast.mainMenu.View

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.mainMenu.Contract.MainMenuContract
import com.cnunez.docufast.mainMenu.Presenter.MainMenuPresenter
import com.cnunez.docufast.useCamera.View.CameraActivity

class MainMenuView : AppCompatActivity(), MainMenuContract.View {
    private lateinit var mainMenuPresenter: MainMenuPresenter
    private lateinit var gotoCameraButton: Button
    private lateinit var gotogalleryButton: Button
    private lateinit var gotoarchivesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainMenuPresenter = MainMenuPresenter(this)

        gotoCameraButton = findViewById(R.id.gotocameraButton)
        gotogalleryButton = findViewById(R.id.gotogalleryButton)
        gotoarchivesButton = findViewById(R.id.gotoarchivesButton)

        gotogalleryButton.setOnClickListener {
            mainMenuPresenter.onGotoGalleryButtonClicked()
        }
        gotoCameraButton.setOnClickListener {
            mainMenuPresenter.onGotoCameraButtonClicked()
        }
    }

    override fun gotouseCameraButton() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    override fun gotogalleryButton() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivity(intent)
    }

    override fun gotoarchivesButton() {
            // Implementación futura
    }
}
