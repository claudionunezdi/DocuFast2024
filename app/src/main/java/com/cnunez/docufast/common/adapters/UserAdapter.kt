package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserAdapter(
    private val users: MutableList<User>,
    private val selectedUsers: MutableList<User> = mutableListOf(),
    private val onUserDeleteClickListener: ((User) -> Unit)? = null
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
        return selectedUsers.toList()

    }

    fun createUser(
        id: String,
        name: String,
        email: String,
        password: String,
        organization: String,
        workGroups: List<String>,
        role: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(
                        id = id,
                        name = name,
                        email = email,
                        password = password,
                        organization = organization,
                        workGroups = workGroups.toMutableList(),
                        role = role
                    )
                    // Store user data in Firestore
                    val userData = hashMapOf(
                        "id" to id,
                        "name" to name,
                        "email" to email,
                        "organization" to organization,
                        "workGroups" to workGroups,
                        "role" to role
                    )
                    db.collection("users").document(id).set(userData)
                        .addOnSuccessListener {
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            onComplete(false, e.message)
                        }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val checkBoxUser: CheckBox = itemView.findViewById(R.id.checkBoxUser)
        //private val buttonDeleteUser: View = itemView.findViewById(R.id.buttonDeleteUser)

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

            //buttonDeleteUser.setOnClickListener {
            //  onUserDeleteClickListener?.invoke(user)
            //}
        }
    }
}