package com.cnunez.docufast.admin.group.detail.view

import com.cnunez.docufast.common.dataclass.WorkGroup

interface GroupDetailContract {
    interface View {
        fun showGroupDetails(group: WorkGroup)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadGroupDetails(groupId: String)
    }
}