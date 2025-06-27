package com.cnunez.docufast.admin.group.detail.contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.User

interface GroupDetailContract {
    interface View {
        fun showProgress()
        fun hideProgress()
        fun showGroupName(name: String)
        fun showMembers(users: List<User>)
        fun showFiles(files: List<File>)
        fun onGroupDeleted()
        fun onError(message: String)
    }

    interface Presenter {
        fun loadGroupDetails(groupId: String)
        fun deleteGroup(groupId: String)
    }

    interface Model {
        suspend fun getGroupMembers(groupId: String): List<User>
        suspend fun getGroupFiles(groupId: String): List<File>
        suspend fun deleteGroup(groupId: String)
    }
}
