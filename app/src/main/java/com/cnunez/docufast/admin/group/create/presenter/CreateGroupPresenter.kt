package com.cnunez.docufast.admin.group.create.presenter

import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.admin.group.create.model.CreateGroupModel
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.Group
import java.io.File

class CreateGroupPresenter(
    private val view: CreateGroupContract.View,
    private val model: CreateGroupModel
) : CreateGroupContract.Presenter {

    override fun createGroup(name: String, description: String, members: List<User>, files: List<File>) {
        try {
            val group = model.createGroup(name, description, members, files)
            view.onGroupCreated(group)
        } catch (e: Exception) {
            view.onError(e.message ?: "Unknown error")
        }
    }
}