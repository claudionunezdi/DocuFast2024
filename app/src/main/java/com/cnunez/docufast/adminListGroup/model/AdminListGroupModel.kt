package com.cnunez.docufast.adminListGroup.model


import com.cnunez.docufast.common.dataclass.WorkGroup
import com.cnunez.docufast.adminListGroup.contract.AdminListGroupContract
import com.cnunez.docufast.adminCreateGroup.model.AdminCreateGroupModel

class AdminListGroupModel : AdminListGroupContract.Model {

    private val adminCreateGroupModel: AdminCreateGroupModel = AdminCreateGroupModel()

    override fun fetchGroups(): List<WorkGroup> {
        return adminCreateGroupModel.getGroups()
    }
}