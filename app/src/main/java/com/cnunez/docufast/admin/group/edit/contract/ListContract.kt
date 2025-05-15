package com.cnunez.docufast.admin.group.edit.contract

import com.cnunez.docufast.common.dataclass.Group

interface ListContract {
    interface View {
        fun showGroups(groups: List<Group>)
        fun showError(message: String)
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