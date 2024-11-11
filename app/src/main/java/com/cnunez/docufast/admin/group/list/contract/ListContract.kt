package com.cnunez.docufast.admin.group.list.contract

import com.cnunez.docufast.common.dataclass.WorkGroup

interface ListContract {
    interface View {
        fun showGroups(groups: List<WorkGroup>)
        fun showError(message: String)
    }

    interface Presenter {
        fun loadGroups()
    }

    interface Model {
        fun fetchGroups(): List<WorkGroup>
    }
}