package com.cnunez.docufast.common.firebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group

class UserGroupAdapter(
    private var groups: List<Group>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<UserGroupAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onGroupClick(group: Group)
    }

    // ViewHolder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.groupNameTextView)
        private val membersTextView: TextView = itemView.findViewById(R.id.groupMembersTextView)

        fun bind(group: Group) {
            nameTextView.text = group.name
            membersTextView.text = "${group.members.size} miembros"

            itemView.setOnClickListener {
                listener.onGroupClick(group)
            }
        }
    }

    // Métodos clave
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_users, parent, false) // ← Cambiado a item_group_users
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    // Actualización eficiente con DiffUtil
    fun setGroups(newGroups: List<Group>) {
        val diffResult = DiffUtil.calculateDiff(GroupDiffCallback(groups, newGroups))
        groups = newGroups
        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil para optimizar rendimiento
    private class GroupDiffCallback(
        private val oldList: List<Group>,
        private val newList: List<Group>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}