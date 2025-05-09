package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.dataclass.User

class UserSelectionAdapter(
    private val users: MutableList<User>,
    private val onUserSelected: ((User, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    fun setUsers(newUsers: List<User>) {
        val oldSize = users.size
        users.clear()
        notifyItemRangeRemoved(0, oldSize)
        users.addAll(newUsers)
        notifyItemRangeInserted(0, newUsers.size)
    }
    fun filter(query: String) {
        val filteredUsers = users.filter { user ->
            user.name.contains(query, ignoreCase = true) ||
                    user.workGroups.any { group -> group.contains(query, ignoreCase = true) } ||
                    user.email.contains(query, ignoreCase = true)
        }
        setUsers(filteredUsers)
    }

    fun setGroups(groups: List<Group>) {
        val newUsers = groups.flatMap { group ->
            group.members.map { user ->
                user.copy(workGroups = mutableListOf(group.name))
            }
        }
        setUsers(newUsers)

    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val textViewUserGroups: TextView = itemView.findViewById(R.id.textViewUserGroups)
        private val checkBoxSelectUser: CheckBox = itemView.findViewById(R.id.checkBoxSelectUser)

        fun bind(user: User) {
            textViewUserName.text = user.name
            textViewUserGroups.text = user.workGroups.joinToString(", ")
            checkBoxSelectUser.isChecked = user.isSelected

            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(user)
            }

            checkBoxSelectUser.setOnCheckedChangeListener { _, isChecked ->
                user.isSelected = isChecked
                onUserSelected?.invoke(user, isChecked)
            }
        }
    }
}