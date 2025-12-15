package com.example.overtimemanagesys_zan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.overtimemanagesys_zan.data.Employee
import com.example.overtimemanagesys_zan.databinding.ItemEmployeeSelectionBinding

class EmployeeSelectionAdapter : ListAdapter<Employee, EmployeeSelectionAdapter.ViewHolder>(EmployeeDiffCallback()) {

    private val selectedIds = mutableSetOf<Long>()

    fun setSelectedIds(ids: Set<Long>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged()
    }

    fun getSelectedIds(): Set<Long> {
        return selectedIds.toSet()
    }

    fun selectAll() {
        selectedIds.clear()
        selectedIds.addAll(currentList.map { it.id })
        notifyDataSetChanged()
    }

    fun deselectAll() {
        selectedIds.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmployeeSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemEmployeeSelectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(employee: Employee) {
            binding.checkBoxEmployee.text = employee.name
            binding.checkBoxEmployee.isChecked = selectedIds.contains(employee.id)

            binding.checkBoxEmployee.setOnClickListener {
                if (binding.checkBoxEmployee.isChecked) {
                    selectedIds.add(employee.id)
                } else {
                    selectedIds.remove(employee.id)
                }
            }
            
            // ルートレイアウト（ConstraintLayout）のクリックでもチェックボックスをトグルできるようにする
            binding.root.setOnClickListener {
                binding.checkBoxEmployee.toggle()
                if (binding.checkBoxEmployee.isChecked) {
                    selectedIds.add(employee.id)
                } else {
                    selectedIds.remove(employee.id)
                }
            }
        }
    }

    class EmployeeDiffCallback : DiffUtil.ItemCallback<Employee>() {
        override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem == newItem
        }
    }
}

