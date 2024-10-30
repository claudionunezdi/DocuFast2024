package com.cnunez.docufast.adminListGroup.presenter

import com.cnunez.docufast.adminListGroup.contract.AdminListGroupContract

class AdminListGroupPresenter(
    private val view: AdminListGroupContract.View,
    private val model: AdminListGroupContract.Model
) : AdminListGroupContract.Presenter {

    override fun loadGroups() {
        try {
            val groups = model.fetchGroups()
            view.showGroups(groups)
        } catch (e: Exception) {
            view.showError("Failed to load groups")
        }
    }
}