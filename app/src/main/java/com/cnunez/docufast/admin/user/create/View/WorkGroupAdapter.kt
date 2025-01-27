package com.cnunez.docufast.admin.user.create.View


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R

class WorkgroupAdapter(private val workgroups: List<String>) : RecyclerView.Adapter<WorkgroupAdapter.WorkgroupViewHolder>() {

    private val selectedWorkgroups = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkgroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workgroup, parent, false)
        return WorkgroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkgroupViewHolder, position: Int) {
        val workgroup = workgroups[position]
        holder.workgroupCheckBox.text = workgroup
        holder.workgroupCheckBox.isChecked = selectedWorkgroups.contains(workgroup)
        holder.workgroupCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedWorkgroups.add(workgroup)
            } else {
                selectedWorkgroups.remove(workgroup)
            }
        }
    }

    override fun getItemCount(): Int = workgroups.size

    fun getSelectedWorkgroups(): List<String> = selectedWorkgroups.toList()

    class WorkgroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workgroupCheckBox: CheckBox = itemView.findViewById(R.id.workgroupCheckBox)
    }
}