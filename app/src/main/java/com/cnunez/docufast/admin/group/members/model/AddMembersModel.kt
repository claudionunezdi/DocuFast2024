package com.cnunez.docufast.admin.group.members.model

import com.cnunez.docufast.admin.group.members.contract.AddMembersContract
import com.cnunez.docufast.common.dataclass.User
import com.cnunez.docufast.common.firebase.GroupDaoRealtime
import com.cnunez.docufast.common.firebase.UserDaoRealtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddMembersModel(
    private val userDao: UserDaoRealtime,
    private val groupDao: GroupDaoRealtime
) : AddMembersContract.Model {

    override suspend fun getAvailableUsers(organizationId: String, groupId: String): List<User> {
        return withContext(Dispatchers.IO) {
            val allUsers = userDao.getByOrganization(organizationId)
            val groupMembers = groupDao.getGroupMembers(groupId).keys

            allUsers.filter { user ->
                user.id !in groupMembers
            }
        }
    }

    override suspend fun addMembers(groupId: String, userIds: List<String>) {
        withContext(Dispatchers.IO) {
            userIds.forEach { userId ->
                groupDao.addMemberToGroup(groupId, userId)
            }
        }
    }
}