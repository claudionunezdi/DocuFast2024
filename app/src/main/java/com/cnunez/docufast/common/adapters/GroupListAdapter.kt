package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group

class GroupListAdapter : ListAdapter<Group, GroupListAdapter.GroupViewHolder>(GroupDiffCallback()) {

    // Interfaz para manejar clicks
    var onItemClickListener: ((Group) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewUserCount: TextView = itemView.findViewById(R.id.textViewUserCount)

        fun bind(group: Group) {
            textViewGroupName.text = group.name
            textViewUserCount.text = itemView.context.getString(
                R.string.user_count_format, // Define este string en tus recursos
                group.members.size
            )

            itemView.setOnClickListener {
                onItemClickListener?.invoke(group)
            }
        }
    }

    private class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }
}