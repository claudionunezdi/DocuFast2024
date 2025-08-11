package com.cnunez.docufast.common.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cnunez.docufast.R

data class UserPickItem(
    val id: String,
    val name: String,
    val email: String,
    var selected: Boolean = false
)

class UserPickAdapter(
    private val items: MutableList<UserPickItem>
) : RecyclerView.Adapter<UserPickAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val nameTv: TextView = view.findViewById(R.id.tvName)
        val emailTv: TextView = view.findViewById(R.id.tvEmail)
        val check: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user_pick, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.nameTv.text = item.name
        holder.emailTv.text = item.email

        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = item.selected

        holder.itemView.setOnClickListener {
            item.selected = !item.selected
            holder.check.isChecked = item.selected
        }
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            item.selected = isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedIds(): List<String> = items.filter { it.selected }.map { it.id }
}
