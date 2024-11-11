package com.cnunez.docufast.admin.group.list.model


import com.cnunez.docufast.common.dataclass.WorkGroup
import com.cnunez.docufast.admin.group.list.contract.ListContract
import com.cnunez.docufast.admin.group.create.model.CreateModel

class ListModel : ListContract.Model {

    private val createModel: CreateModel = CreateModel()

    override fun fetchGroups(): List<WorkGroup> {
        return createModel.getGroups()
    }
}