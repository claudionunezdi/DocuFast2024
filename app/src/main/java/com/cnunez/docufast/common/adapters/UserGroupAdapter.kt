package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group

class UserGroupAdapter(
    private var groups: List<Group> = emptyList(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<UserGroupAdapter.UserGroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return UserGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserGroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    /** Actualiza la lista de grupos y refresca la vista */
    fun setGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }

    inner class UserGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewMemberCount: TextView = itemView.findViewById(R.id.textViewUserCount)

        fun bind(group: Group) {
            textViewGroupName.text = group.name
            // Mostrar cantidad de miembros y archivos
            textViewMemberCount.text = itemView.context.getString(
                R.string.member_and_file_count,
                group.members.size,
                group.files.size
            )
            itemView.setOnClickListener {
                listener.onGroupClick(group)
            }
        }
    }

    interface OnItemClickListener {
        fun onGroupClick(group: Group)
    }
}
