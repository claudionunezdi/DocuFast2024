package com.cnunez.docufast.admin.group.create.presenter

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.admin.group.create.contract.CreateContract
import com.cnunez.docufast.admin.group.create.model.CreateModel
import java.io.File

class CreatePresenter(private val view: CreateContract.View) : CreateContract.Presenter {

    private val model = CreateModel()

    override fun createGroup(name: String, description: String, members: List<User>) {
        if (name.isBlank() || description.isBlank()) {
            view.onError("Name and description cannot be empty")
            return
        }

        // Assuming you have a list of files to pass
        val files = listOf<File>() // Replace with actual list of files if available

        val group = model.createGroup(name, description, members, files)
        view.onGroupCreated(group)
    }
}