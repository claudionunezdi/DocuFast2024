package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User

class UserAdapterUnified(
    private val users: MutableList<User> = mutableListOf(),
    private val mode: Mode = Mode.VIEW,
    private val onUserAction: OnUserActionListener? = null
) : RecyclerView.Adapter<UserAdapterUnified.UserViewHolder>() {

    interface OnUserActionListener {
        fun onUserSelected(user: User, isSelected: Boolean)
        fun onUserClicked(user: User)
        fun onEditUser(user: User)
        fun onDeleteUser(user: User)
    }

    enum class Mode {
        VIEW, EDITABLE, SELECTION
    }

    fun updateUsers(newUsers: List<User>) {
        val previousSelectedIds = users.filter { it.isSelected }.map { it.id }
        users.apply {
            clear()
            addAll(newUsers.map { user ->
                user.copy(isSelected = user.id in previousSelectedIds)
            })
        }
        notifyItemRangeChanged(0, users.size)
    }

    fun getSelectedUsers(): List<User> = users.filter { it.isSelected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutRes = when (mode) {
            Mode.SELECTION -> R.layout.item_user_selection
            Mode.EDITABLE -> R.layout.item_user_editable
            Mode.VIEW -> R.layout.item_user
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return UserViewHolder(view, mode)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View, private val mode: Mode) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.textViewUserName)
        private val emailTv: TextView? = itemView.findViewById(R.id.textViewUserEmail)
        private val groupsTv: TextView? = itemView.findViewById(R.id.textViewUserGroups)
        private val checkBox: CheckBox? = itemView.findViewById(R.id.checkBoxSelectUser)
        private val editBtn: Button? = itemView.findViewById(R.id.buttonEditUser)
        private val deleteBtn: Button? = itemView.findViewById(R.id.buttonDeleteUser)

        fun bind(user: User) {
            nameTv.text = user.name
            emailTv?.text = user.email
            groupsTv?.text = user.workGroups?.keys?.joinToString(", ") ?: ""

            when (mode) {
                Mode.SELECTION -> setupSelectionMode(user)
                Mode.EDITABLE -> setupEditableMode(user)
                Mode.VIEW -> setupViewMode(user)
            }
        }

        private fun setupSelectionMode(user: User) {
            checkBox?.apply {
                visibility = View.VISIBLE
                setOnCheckedChangeListener(null)
                isChecked = user.isSelected
                setOnCheckedChangeListener { _, isChecked ->
                    user.isSelected = isChecked
                    onUserAction?.onUserSelected(user, isChecked)
                }
            }
            itemView.setOnClickListener {
                checkBox?.isChecked = !(checkBox?.isChecked ?: false)
            }
        }

        private fun setupEditableMode(user: User) {
            editBtn?.setOnClickListener { onUserAction?.onEditUser(user) }
            deleteBtn?.setOnClickListener { onUserAction?.onDeleteUser(user) }
            itemView.setOnClickListener { onUserAction?.onUserClicked(user) }
        }

        private fun setupViewMode(user: User) {
            itemView.setOnClickListener { onUserAction?.onUserClicked(user) }
        }
    }

    companion object {
        fun forSelection(users: List<User> = emptyList(), listener: (User, Boolean) -> Unit) =
            UserAdapterUnified(users.toMutableList(), Mode.SELECTION, createSelectionListener(listener))

        fun forView(users: List<User> = emptyList(), onClick: (User) -> Unit) =
            UserAdapterUnified(users.toMutableList(), Mode.VIEW, createViewListener(onClick))

        fun forEditing(
            users: List<User> = emptyList(),
            onEdit: (User) -> Unit,
            onDelete: (User) -> Unit,
            onClick: (User) -> Unit = {}
        ) = UserAdapterUnified(users.toMutableList(), Mode.EDITABLE, createEditableListener(onEdit, onDelete, onClick))

        private fun createSelectionListener(listener: (User, Boolean) -> Unit) =
            object : OnUserActionListener {
                override fun onUserSelected(user: User, isSelected: Boolean) = listener(user, isSelected)
                override fun onUserClicked(user: User) {}
                override fun onEditUser(user: User) {}
                override fun onDeleteUser(user: User) {}
            }

        private fun createViewListener(onClick: (User) -> Unit) =
            object : OnUserActionListener {
                override fun onUserSelected(user: User, isSelected: Boolean) {}
                override fun onUserClicked(user: User) = onClick(user)
                override fun onEditUser(user: User) {}
                override fun onDeleteUser(user: User) {}
            }

        private fun createEditableListener(
            onEdit: (User) -> Unit,
            onDelete: (User) -> Unit,
            onClick: (User) -> Unit
        ) = object : OnUserActionListener {
            override fun onUserSelected(user: User, isSelected: Boolean) {}
            override fun onUserClicked(user: User) = onClick(user)
            override fun onEditUser(user: User) = onEdit(user)
            override fun onDeleteUser(user: User) = onDelete(user)
        }
    }
}