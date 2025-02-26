package com.cnunez.docufast.adapters

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.CheckBox
    import androidx.recyclerview.widget.RecyclerView
    import com.cnunez.docufast.R

    class WorkGroupAdapter(private var workgroups: List<String>)
        :RecyclerView.Adapter<WorkGroupAdapter.WorkgroupViewHolder>() {

        private val selectedWorkgroups = mutableSetOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkgroupViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workgroup, parent, false)
            return WorkgroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: WorkgroupViewHolder, position: Int) {
            val workgroup = workgroups[position]
            holder.bind(workgroup)
        }

        override fun getItemCount(): Int = workgroups.size

        fun getSelectedWorkgroups(): List<String> = selectedWorkgroups.toList()

        fun updateWorkgroups(newWorkgroups: List<String>) {
            workgroups = newWorkgroups
            notifyDataSetChanged()
        }

        inner class WorkgroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val workgroupCheckBox: CheckBox = itemView.findViewById(R.id.workgroupCheckBox)

            fun bind(workgroup: String) {
                workgroupCheckBox.text = workgroup
                workgroupCheckBox.isChecked = selectedWorkgroups.contains(workgroup)
                workgroupCheckBox.setOnCheckedChangeListener(null)
                workgroupCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedWorkgroups.add(workgroup)
                    } else {
                        selectedWorkgroups.remove(workgroup)
                    }
                }
            }
        }
    }