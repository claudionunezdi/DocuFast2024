package com.cnunez.docufast.adminCreateGroup.presenter

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.adminCreateGroup.contract.AdminCreateGroupContract
import com.cnunez.docufast.adminCreateGroup.model.AdminCreateGroupModel

class AdminCreateGroupAdminPresenter(private val view: AdminCreateGroupContract.View) : AdminCreateGroupContract.Presenter {

    private val model = AdminCreateGroupModel()

    override fun createGroup(name: String, description: String, members: List<User>) {
        if (name.isBlank() || description.isBlank()) {
            view.onError("Name and description cannot be empty")
            return
        }

        // Assuming you have a list of files to pass
        val files = listOf<String>() // Replace with actual list of files if available

        val group = model.createGroup(name, description, members, files)
        view.onGroupCreated(group)
    }
}