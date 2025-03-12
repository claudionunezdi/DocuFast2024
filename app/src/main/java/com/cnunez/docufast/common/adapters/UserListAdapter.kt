package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.admin.user.list.presenter.UserListPresenter
import com.cnunez.docufast.common.dataclass.User
import android.widget.Button
import android.widget.TextView
import com.cnunez.docufast.R

class UserListAdapter(
    private var users: MutableList<User>,
    private val presenter: UserListPresenter
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

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.userName.text = user.name
        holder.userGroup.text = user.workGroups.joinToString(", ")
        holder.userEmail.text = user.email

        holder.editButton.setOnClickListener {
            // Handle edit user action
        }

        holder.deleteButton.setOnClickListener {
            presenter.deleteUser(user.id)
        }
    }

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}