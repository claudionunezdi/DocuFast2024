package com.cnunez.docufast.common.adapters
/*

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User

class UserSelectionAdapter(
    private var users: List<User>,
    private val onUserSelected: (User, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder>() {

    private val selectedUsers = mutableListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun setUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<User> = users.filter { it.isSelected }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxSelectUser)
        private val nameTv: TextView = itemView.findViewById(R.id.textViewUserName)
        private val groupsTv: TextView = itemView.findViewById(R.id.textViewUserGroups)

        fun bind(user: User) {
            nameTv.text = user.name
            groupsTv.text = user.workGroups.keys.joinToString(", ")

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = user.isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                user.isSelected = isChecked
                onUserSelected(user, isChecked)
            }

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }
}
*/