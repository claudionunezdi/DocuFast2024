package com.cnunez.docufast.admin.group.detail.view

import com.cnunez.docufast.common.dataclass.Group

interface GroupDetailContract {
    interface View {
        fun showGroupDetails(group: Group)
        fun showError(message: String)
    }

    interface Model {
        interface OnGroupDetailListener {
            fun onSuccess(group: Group)
            fun onError(message: String)
        }

        fun getGroupDetails(groupId: String, listener: OnGroupDetailListener)
    }

    interface Presenter {
        fun loadGroupDetails(groupId: String)
    }
}