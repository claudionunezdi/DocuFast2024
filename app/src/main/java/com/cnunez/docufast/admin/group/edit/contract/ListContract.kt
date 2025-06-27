package com.cnunez.docufast.admin.group.edit.contract

import com.cnunez.docufast.common.dataclass.Group

interface ListContract {
    interface View {
        fun showProgress()
        fun hideProgress()
        fun showGroups(groups: List<Group>)
        fun showError(message: String)
        fun onOpenGoupClick(group:Group)
        fun onGroupClick(group:Group)
        fun onDeleteClick(group:Group)
    }

    interface Presenter {
        fun loadGroups()
        fun deleteGroup(groupId: String)
    }

    interface Model {
        fun fetchGroups(callback: (List<Group>?, String?) -> Unit)
        fun deleteGroup(groupId: String, callback: (Boolean, String?) -> Unit)
    }
}
