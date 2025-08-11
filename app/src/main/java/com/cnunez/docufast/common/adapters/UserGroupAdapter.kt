package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group


////@Funcion muestra los grupos de un usuario

class UserGroupAdapter(
    private var groups: List<Group> = emptyList(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<UserGroupAdapter.UserGroupViewHolder>() {

    // Mantenemos el método original para compatibilidad
    fun setGroups(newGroups: List<Group>) {
        groups = newGroups.sortedBy { it.name }
        notifyDataSetChanged()
    }

    // Nuevo método con mejor nombre
    fun updateGroups(newGroups: List<Group>) {
        setGroups(newGroups)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_user, parent, false) // Usamos el nuevo layout
        return UserGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserGroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    inner class UserGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewGroupDescription)
        private val textViewMemberCount: TextView = itemView.findViewById(R.id.textViewMemberCount)
        private val textViewFileCount: TextView = itemView.findViewById(R.id.textViewFileCount)

        fun bind(group: Group) {
            // Información básica
            textViewGroupName.text = group.name
            textViewDescription.text = group.description.ifEmpty {
                itemView.context.getString(R.string.no_description)
            }

            // Contadores
            textViewMemberCount.text = itemView.context.getString(
                R.string.member_count,
                group.members.size
            )
            textViewFileCount.text = itemView.context.getString(
                R.string.file_count,
                group.files.size
            )

            // Click listener (manteniendo compatibilidad)
            cardView.setOnClickListener {
                listener.onGroupClick(group)
            }
        }
    }

    // Mantenemos la interfaz original para no romper código existente
    interface OnItemClickListener {
        fun onGroupClick(group: Group)
    }

    // Nueva interfaz extendida (opcional para futuras features)
    interface EnhancedClickListener : OnItemClickListener {
        fun onGroupLongClick(group: Group) // Ejemplo de nueva funcionalidad
    }
}