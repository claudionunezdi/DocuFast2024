package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User

class UserListAdapter(
    private var users: List<User> = emptyList(),
    private val onEditClickListener: ((User) -> Unit)? = null,
    private val onDeleteClickListener: ((User) -> Unit)? = null
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    /** Reemplaza la lista de usuarios y refresca la vista */
    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val userGroups: TextView = itemView.findViewById(R.id.textViewUserGroups)
        private val userEmail: TextView = itemView.findViewById(R.id.textViewUserEmail)
        private val editButton: Button = itemView.findViewById(R.id.buttonEditUser)
        private val deleteButton: Button = itemView.findViewById(R.id.buttonDeleteUser)

        fun bind(user: User) {
            userName.text = user.name
            userGroups.text = user.workGroups.keys.joinToString(", ")
            userEmail.text = user.email

            editButton.setOnClickListener {
                onEditClickListener?.invoke(user)
            }
            deleteButton.setOnClickListener {
                onDeleteClickListener?.invoke(user)
            }
        }
    }
}
