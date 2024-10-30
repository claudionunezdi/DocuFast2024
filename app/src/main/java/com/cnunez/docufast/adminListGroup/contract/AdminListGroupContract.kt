package com.cnunez.docufast.adminListGroup.contract

import com.cnunez.docufast.common.dataclass.WorkGroup

interface AdminListGroupContract {
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