package com.cnunez.docufast.user.group.detail.contract

import com.cnunez.docufast.common.dataclass.TextFile

interface GroupDetailContract {
    interface View {
        fun showFiles(files: List<TextFile>)
        fun showError(message: String)
        fun showFileDetail(file: TextFile)
    }

    interface Presenter {
        fun loadGroupFiles(groupId: String, organizationId: String) // Add organizationId parameter
        fun onFileSelected(file: TextFile)
    }

    interface Model {
        fun fetchGroupFiles(groupId: String, organizationId: String, callback: (List<TextFile>?, String?) -> Unit) // Add organizationId parameter
    }
}