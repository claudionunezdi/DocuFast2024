package com.cnunez.docufast.user.mainmenu.Contract

import com.cnunez.docufast.common.dataclass.File
import com.cnunez.docufast.common.dataclass.Group

interface MainMenuUserContract{
    interface View {
        fun showGroups(groups: List<Group>)
        fun showError(message: String)
        fun showGroupDetail(group: Group)
    }

    interface Presenter {
        fun loadUserGroups()
        fun onGroupSelected(group: Group)
    }

    interface Model {
        fun fetchUserGroups(userId: String, callback: (List<Group>?, String?) -> Unit)
        fun fetchGroupFiles(groupId: String, callback: (List<File>?, String?) -> Unit)
    }

}