package com.cnunez.docufast.admin.group.list.presenter

import com.cnunez.docufast.admin.group.list.contract.ListContract

class ListPresenter(
    private val view: ListContract.View,
    private val model: ListContract.Model
) : ListContract.Presenter {

    override fun loadGroups() {
        try {
            val groups = model.fetchGroups()
            view.showGroups(groups)
        } catch (e: Exception) {
            view.showError("Failed to load groups")
        }
    }
}