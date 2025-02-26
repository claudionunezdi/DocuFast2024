package com.cnunez.docufast.admin.group.create.contract

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup
import java.io.File

interface CreateGroupContract {
    interface View {
        fun onGroupCreated(group: WorkGroup)
        fun onError(message: String)
        fun getUsersFromOrganization(organization: String)
    }

    interface Presenter {
        fun createGroup(name: String, description: String, members: List<User>, files: List<File>)
    }
}