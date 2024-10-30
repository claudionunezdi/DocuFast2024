package com.cnunez.docufast.adminCreateGroup.model

import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.WorkGroup

class AdminCreateGroupModel {

    private val groups = mutableListOf<WorkGroup>()

    fun createGroup(name: String, description: String, members: List<User>, files: List<String>): WorkGroup {
        val group = WorkGroup(id = groups.size + 1, name = name, description = description, members = members, files = files)
        groups.add(group)
        return group
    }

    fun getGroups(): List<WorkGroup> {
        return groups
    }
}