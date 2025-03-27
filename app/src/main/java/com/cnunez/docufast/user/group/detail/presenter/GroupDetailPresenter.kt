package com.cnunez.docufast.user.group.detail.presenter

import com.cnunez.docufast.common.dataclass.TextFile
import com.cnunez.docufast.user.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.user.group.detail.model.GroupDetailModel

class GroupDetailPresenter(
    private val view: GroupDetailContract.View,
    private val model: GroupDetailContract.Model = GroupDetailModel()
) : GroupDetailContract.Presenter {

    override fun loadGroupFiles(groupId: String, organizationId: String) {
        model.fetchGroupFiles(groupId, organizationId) { files, error ->
            if (error != null) {
                view.showError(error)
            } else if (files != null) {
                view.showFiles(files)
            }
        }
    }

    override fun onFileSelected(file: TextFile) {
        view.showFileDetail(file)
    }
}