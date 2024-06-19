package com.cnunez.docufast.archives.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cnunez.docufast.archives.Contract.ArchivesContract
import com.cnunez.docufast.archives.Presenter.ArchivesPresenter


class ArchivesActivity: AppCompatActivity(), ArchivesContract.View {



    private lateinit var presenter: ArchivesContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        presenter = ArchivesPresenter()



    }

    override fun listFiles(){


    }

    override fun editFile(){

    }


}