package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group

class GroupListAdapter(
    private val groups: MutableList<Group>
) : RecyclerView.Adapter<GroupListAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int = groups.size

    fun setGroups(newGroups: List<Group>) {
        val oldSize = groups.size
        groups.clear()
        notifyItemRangeRemoved(0, oldSize)
        groups.addAll(newGroups)
        notifyItemRangeInserted(0, newGroups.size)
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewUserCount: TextView = itemView.findViewById(R.id.textViewUserCount)

        fun bind(group: Group) {
            textViewGroupName.text = group.name
            textViewUserCount.text = "Usuarios = ${group.members.size}"
        }
    }
}