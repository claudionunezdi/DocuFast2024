package com.cnunez.docufast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R
import com.cnunez.docufast.common.dataclass.WorkGroup
import android.widget.Filter
import android.widget.Filterable

class GroupAdapter(
    private var groups: List<WorkGroup>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>(), Filterable {

    private var filteredGroups: List<WorkGroup> = groups

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = filteredGroups[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return filteredGroups.size
    }

    fun setGroups(groups: List<WorkGroup>) {
        this.filteredGroups = groups
        notifyDataSetChanged()
    }

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
                filteredGroups = results?.values as List<WorkGroup>
                notifyDataSetChanged()
            }
        }
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val textViewGroupName: TextView = itemView.findViewById(R.id.textViewGroupName)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(group: WorkGroup) {
            textViewGroupName.text = group.name
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(filteredGroups[position])
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(group: WorkGroup)
    }
}