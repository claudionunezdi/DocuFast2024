package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User

class UserAdapter(
    private val users: MutableList<User>,
    private val onEditClickListener: ((User) -> Unit)? = null,
    private val onDeleteClickListener: ((User) -> Unit)? = null
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
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<User> {
        return users.filter { it.isSelected }


    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val textViewUserGroups: TextView = itemView.findViewById(R.id.textViewUserGroups)
        private val textViewUserEmail: TextView = itemView.findViewById(R.id.textViewUserEmail)
        private val buttonEditUser: Button = itemView.findViewById(R.id.buttonEditUser)
        private val buttonDeleteUser: Button = itemView.findViewById(R.id.buttonDeleteUser)

        fun bind(user: User) {
            textViewUserName.text = user.name
            textViewUserGroups.text = user.workGroups.joinToString(", ")
            textViewUserEmail.text = user.email

            buttonEditUser.setOnClickListener {
                onEditClickListener?.invoke(user)
            }

            buttonDeleteUser.setOnClickListener {
                onDeleteClickListener?.invoke(user)
            }
        }
    }
}