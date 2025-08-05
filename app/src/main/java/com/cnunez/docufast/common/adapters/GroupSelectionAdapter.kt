package com.cnunez.docufast.common.adapters

import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.common.dataclass.Group
import com.cnunez.docufast.databinding.ItemGroupSelectedBinding

class GroupSelectionAdapter(
    private var groups: List<Group> = emptyList(),
    private var selectedGroupIds: Set<String> = emptySet()
) : RecyclerView.Adapter<GroupSelectionAdapter.GroupViewHolder>() {

    private val selectedGroups = mutableSetOf<String>()
    private var onGroupSelectedListener: ((List<String>) -> Unit)? = null

    // Métodos públicos
    fun getSelectedGroups(): List<String> = selectedGroups.toList()

    fun clearSelection() {
        selectedGroups.clear()
        notifyItemRangeChanged(0, itemCount) // Más eficiente que notifyDataSetChanged
    }

    fun setOnGroupSelectedListener(listener: (List<String>) -> Unit) {
        this.onGroupSelectedListener = listener
    }

    fun updateGroups(newGroups: List<Group>, newSelectedIds: Set<String> = emptySet()) {
        groups = newGroups.sortedBy { it.name }
        selectedGroupIds = newSelectedIds
        notifyDataSetChanged()
    }

    fun updateGroups(newGroups: List<Group>) {
        updateGroups(newGroups, emptySet())
    }

    // ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupSelectedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            holder.bind(groups[position])
        } else {
            Log.e("GroupAdapter", "¡Se intentó bindear en hilo secundario!")
        }
    }

    override fun getItemCount(): Int = groups.size

    inner class GroupViewHolder(
        private val binding: ItemGroupSelectedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group) {
            binding.apply {
                // Configurar vistas
                textViewGroupName.text = group.name ?: "Sin nombre"
                textViewUserCount.text = "Miembros: ${group.members?.size ?: 0}"
                textViewDescription.text = group.description ?: "Sin descripción" // Añadido

                // Manejar selección
                checkboxSelected.setOnCheckedChangeListener(null)
                checkboxSelected.isChecked = selectedGroups.contains(group.id)

                // Listeners optimizados
                root.setOnClickListener {
                    checkboxSelected.toggle()
                }

                checkboxSelected.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedGroups.add(group.id)
                    } else {
                        selectedGroups.remove(group.id)
                    }
                    onGroupSelectedListener?.invoke(selectedGroups.toList())
                }
            }
        }
    }
}