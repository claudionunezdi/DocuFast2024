package com.cnunez.docufast.common.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.admin.user.edit.view.UserDetailActivity
import com.cnunez.docufast.common.dataclass.User

class UserListAdapter(
    private var users: MutableList<User>,
    private val onEditClickListener: ((User) -> Unit)? = null,
    private val onDeleteClickListener: ((User) -> Unit)? = null
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.textViewUserName)
        val userGroup: TextView = view.findViewById(R.id.textViewUserGroups)
        val userEmail: TextView = view.findViewById(R.id.textViewUserEmail)
        val editButton: Button = view.findViewById(R.id.buttonEditUser)
        val deleteButton: Button = view.findViewById(R.id.buttonDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.userName.text = user.name
        holder.userGroup.text = user.workGroups.joinToString(", ")
        holder.userEmail.text = user.email

        holder.editButton.setOnClickListener {
            onEditClickListener?.invoke(user)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClickListener?.invoke(user)
        }
    }

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}