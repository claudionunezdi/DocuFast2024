package com.cnunez.docufast.user.mainmenu.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.R
import com.cnunez.docufast.user.mainmenu.Contract.MainMenuUserContract
import com.cnunez.docufast.user.mainmenu.Presenter.MainMenuUserPresenter
import com.cnunez.docufast.user.workgroup.menu.View.WorkGroupsActivity

class MainMenuUserActivity : AppCompatActivity(), MainMenuUserContract.View {

    private lateinit var presenter: MainMenuUserContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_user)

        presenter = MainMenuUserPresenter(this)

        findViewById<Button>(R.id.viewWorkGroupsButton).setOnClickListener {
            presenter.onViewWorkGroupsClicked()
        }


    }

    override fun showWorkGroups() {

        intent = Intent(this, WorkGroupsActivity::class.java)
        startActivity(intent)


    }

}