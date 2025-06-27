package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.common.manager.GroupManager
import android.widget.Filter
import android.widget.Filterable

class GroupAdapter(
    private var groups: List<Group>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>(), Filterable {

    private var filteredGroups: List<Group> = groups.sortedBy { it.name }
    private val groupManager = GroupManager()

    fun setGroups(groups: List<Group>) {
        this.groups = groups.sortedBy { it.name }
        this.filteredGroups = this.groups
        notifyDataSetChanged()
    }

    fun removeGroup(group: Group) {
        val index = filteredGroups.indexOf(group)
        if (index != -1) {
            filteredGroups = filteredGroups.toMutableList().apply { removeAt(index) }
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(filteredGroups[position])
    }

    override fun getItemCount(): Int = filteredGroups.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filteredList = if (query.isEmpty()) {
                    groups
                } else {
                    groups.filter {
                        it.name.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredGroups = results?.values as List<Group>
                notifyDataSetChanged()
            }
        }
    }


    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)
        private val textViewUserCount: TextView = itemView.findViewById(R.id.textViewUserCount)
        private val buttonOpenGroup: ImageButton = itemView.findViewById(R.id.buttonOpenGroup)
        private val buttonDeleteGroup: ImageButton = itemView.findViewById(R.id.buttonDeleteGroup)

        fun bind(group: Group) {
            textViewGroupName.text = group.name
            textViewUserCount.text =
                itemView.context.getString(R.string.user_count, group.members.size)
            buttonOpenGroup.setOnClickListener {
                listener.onOpenGroupClick(group)
            }
            buttonDeleteGroup.setOnClickListener {
                groupManager.deleteGroup(group.id) { success, error ->
                    if (success) {
                        listener.onDeleteGroupClick(group)
                    } else {
                        // Manejar el error
                        error?.let {
                            // Manejar el error
                        }

                    }
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onOpenGroupClick(group: Group)
        fun onGroupClick(group: Group)
        fun  onDeleteClick(group: Group)
        fun onDeleteGroupClick(group: Group)
    }
}