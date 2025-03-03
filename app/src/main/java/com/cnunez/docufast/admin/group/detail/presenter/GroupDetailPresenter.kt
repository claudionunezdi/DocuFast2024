package com.cnunez.docufast.admin.group.detail.view

import com.cnunez.docufast.admin.group.detail.model.GroupDetailModel
import com.cnunez.docufast.common.dataclass.Group

class GroupDetailPresenter(
    private val view: GroupDetailContract.View,
    private val model: GroupDetailContract.Model
) : GroupDetailContract.Presenter {

    override fun loadGroupDetails(groupId: String) {
        model.getGroupDetails(groupId, object : GroupDetailContract.Model.OnGroupDetailListener {
            override fun onSuccess(group: Group) {
                view.showGroupDetails(group)
            }

            override fun onError(message: String) {
                view.showError(message)
            }
        })
    }
}