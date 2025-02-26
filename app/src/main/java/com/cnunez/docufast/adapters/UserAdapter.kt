package com.cnunez.docufast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User

class UserAdapter(
    private val users: MutableList<User>,
    private val selectedUsers: MutableList<User> = mutableListOf()
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    fun setUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyItemRangeChanged(0, newUsers.size)
    }

    fun addUser(user: User) {
        users.add(user)
        notifyItemInserted(users.size - 1)
    }

    fun removeUser(user: User) {
        val position = users.indexOf(user)
        if (position != -1) {
            users.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateUser(user: User) {
        val position = users.indexOf(user)
        if (position != -1) {
            users[position] = user
            notifyItemChanged(position)
        }
    }

    fun getSelectedUsers(): List<User> {
        return selectedUsers.toList()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val checkBoxUser: CheckBox = itemView.findViewById(R.id.checkBoxUser)

        fun bind(user: User) {
            textViewUserName.text = user.name
            checkBoxUser.isChecked = selectedUsers.contains(user)
            checkBoxUser.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUsers.add(user)
                } else {
                    selectedUsers.remove(user)
                }
            }

            itemView.setOnClickListener {
                checkBoxUser.isChecked = !checkBoxUser.isChecked
            }
        }
    }
}