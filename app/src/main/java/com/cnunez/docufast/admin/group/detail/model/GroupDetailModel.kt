package com.cnunez.docufast.admin.group.detail.model

import com.cnunez.docufast.admin.group.detail.contract.GroupDetailContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import com.cnunez.docufast.common.firebase.FileDaoRealtime
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupDetailModel(
    private val userDao: UserDaoRealtime,
    private val fileDao: FileDaoRealtime,
    private val groupDao: GroupDaoRealtime
) : GroupDetailContract.Model {

    override suspend fun getGroupMembers(groupId: String): List<User> = withContext(Dispatchers.IO) {
        val group = groupDao.getGroupByIdSuspend(groupId) ?: return@withContext emptyList()
        group.members.keys.mapNotNull { userDao.getById(it) }
    }

    override suspend fun getGroupFiles(groupId: String): List<File> = withContext(Dispatchers.IO) {
        fileDao.getAll().filter { it.groupId == groupId }
    }

    override suspend fun deleteGroup(groupId: String) = withContext(Dispatchers.IO) {
        groupDao.deleteGroupSuspend(groupId)
    }
}
