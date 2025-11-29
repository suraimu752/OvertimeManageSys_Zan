package com.example.overtimemanagesys_zan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.overtimemanagesys_zan.EmployeeOvertimeItem
import com.example.overtimemanagesys_zan.databinding.ItemDateOvertimeBinding

class DateOvertimeListAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<EmployeeOvertimeItem, DateOvertimeListAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemDateOvertimeBinding,
        private val onItemClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EmployeeOvertimeItem) {
            binding.textViewEmployeeName.text = item.employeeName
            binding.textViewHours.text = "${item.hours}h"
            
            binding.root.setOnClickListener {
                onItemClick(item.employeeId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDateOvertimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<EmployeeOvertimeItem>() {
        override fun areItemsTheSame(oldItem: EmployeeOvertimeItem, newItem: EmployeeOvertimeItem): Boolean {
            return oldItem.employeeId == newItem.employeeId
        }

        override fun areContentsTheSame(oldItem: EmployeeOvertimeItem, newItem: EmployeeOvertimeItem): Boolean {
            return oldItem == newItem
        }
    }
}

