package com.example.overtimemanagesys_zan.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.overtimemanagesys_zan.databinding.ItemCalendarDateBinding

data class CalendarDateItem(
    val date: String, // YYYY-MM-DD
    val day: Int,
    val hours: Double,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val isFuture: Boolean = false
)

class CalendarAdapter(
    private val onDateClick: (String) -> Unit
) : ListAdapter<CalendarDateItem, CalendarAdapter.CalendarViewHolder>(CalendarDiffCallback()) {

    class CalendarViewHolder(
        private val binding: ItemCalendarDateBinding,
        private val onDateClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDateItem) {
            
            binding.textViewDate.text = if (item.isCurrentMonth) item.day.toString() else ""
            
            // 残業時間の表示：小数点以下がない場合は整数で、0の場合は非表示
            val hoursText = when {
                item.hours <= 0 -> ""
                item.hours % 1.0 == 0.0 -> item.hours.toInt().toString()
                else -> item.hours.toString()
            }
            binding.textViewHours.text = hoursText

            // クリックリスナーを確実に設定（古いリスナーをクリア）
            binding.root.setOnClickListener(null)
            binding.root.isClickable = true
            binding.root.isFocusable = true
            
            // 未来の日付もクリック可能に変更（グレーアウトは維持）
            val isClickable = item.isCurrentMonth
            binding.root.isClickable = isClickable
            binding.root.isFocusable = isClickable
            
            if (isClickable) {
                binding.root.setOnClickListener {
                    onDateClick(item.date)
                }
            }

            // 表示の調整
            if (!item.isCurrentMonth) {
                // 現在の月以外は薄く表示
                binding.textViewDate.alpha = 0.3f
                binding.textViewHours.alpha = 0.3f
            } else if (item.isFuture) {
                // 未来の日付はグレーアウト
                binding.textViewDate.alpha = 0.5f
                binding.textViewHours.alpha = 0.5f
            } else {
                // 通常の日付
                binding.textViewDate.alpha = 1.0f
                binding.textViewHours.alpha = 1.0f
            }
            
            // 今日の日付をハイライト（背景色を変更）
            if (item.isToday) {
                binding.root.setBackgroundResource(com.example.overtimemanagesys_zan.R.drawable.calendar_border_today)
            } else {
                binding.root.setBackgroundResource(com.example.overtimemanagesys_zan.R.drawable.calendar_border)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarViewHolder(binding, onDateClick)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CalendarDiffCallback : DiffUtil.ItemCallback<CalendarDateItem>() {
        override fun areItemsTheSame(oldItem: CalendarDateItem, newItem: CalendarDateItem): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDateItem, newItem: CalendarDateItem): Boolean {
            return oldItem == newItem
        }
    }
}

