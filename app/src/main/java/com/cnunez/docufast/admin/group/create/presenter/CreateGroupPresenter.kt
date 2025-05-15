package com.cnunez.docufast.admin.group.create.presenter

import com.cnunez.docufast.admin.group.create.contract.CreateGroupContract
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User
import java.util.UUID

class CreateGroupPresenter(
    private val view: CreateGroupContract.View,
    private val model: CreateGroupContract.Model
) : CreateGroupContract.Presenter {

    override fun createGroup(name: String, description: String, members: List<User>) {
        if (name.isBlank() || description.isBlank()) {
            view.onError("Nombre y descripción son requeridos")
            return
        }

        val groupId = UUID.randomUUID().toString()
        val group = Group(
            id = groupId,
            name = name,
            description = description,
            members = members.toMutableList()
        )

        model.saveGroup(
            group = group,
            onSuccess = { view.onGroupCreated(group) },
            onFailure = { e -> view.onError(e.message ?: "Error al crear grupo") }
        )
    }
}