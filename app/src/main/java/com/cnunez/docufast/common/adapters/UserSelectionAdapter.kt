package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.User

/**
 * Adapter para mostrar una lista de usuarios con un CheckBox de selección.
 * @param users Lista inicial de usuarios.
 * @param onUserSelected Callback invocado al (des)seleccionar un usuario.
 */
class UserSelectionAdapter(
    private var users: List<User> = emptyList(),
    private val onUserSelected: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    /**
     * Reemplaza la lista de usuarios y refresca el RecyclerView.
     */
    fun setUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxSelectUser)
        private val nameTv: TextView = itemView.findViewById(R.id.textViewUserName)
        private val groupsTv: TextView = itemView.findViewById(R.id.textViewUserGroups)

        fun bind(user: User) {
            // Nombre y grupos
            nameTv.text = user.name
            groupsTv.text = user.workGroups.keys.joinToString(", ")

            // Evitar disparar el listener al reciclar
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = user.isSelected

            // Listener para cambios de selección
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                user.isSelected = isChecked
                onUserSelected(user, isChecked)
            }

            // Alternativa: permitir selección haciendo clic en todo el item
            itemView.setOnClickListener {
                val newState = !user.isSelected
                checkBox.isChecked = newState
            }
        }
    }
}
