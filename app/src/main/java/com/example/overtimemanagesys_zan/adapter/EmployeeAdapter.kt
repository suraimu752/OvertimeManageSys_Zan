package com.example.overtimemanagesys_zan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.overtimemanagesys_zan.data.Employee
import com.example.overtimemanagesys_zan.data.EmployeeWithOvertime
import com.example.overtimemanagesys_zan.databinding.ItemEmployeeBinding
import android.graphics.Color
import android.animation.ArgbEvaluator
import android.graphics.drawable.GradientDrawable
import androidx.cardview.widget.CardView

class EmployeeAdapter(
    private val onThisMonthClick: (Employee) -> Unit,
    private val onNameLongClick: ((Employee) -> Unit)? = null
) : ListAdapter<EmployeeWithOvertime, EmployeeAdapter.EmployeeViewHolder>(EmployeeDiffCallback()) {

    private var sortMode: Boolean = false

    class EmployeeViewHolder(
        private val binding: ItemEmployeeBinding,
        private val onThisMonthClick: (Employee) -> Unit,
        private val onNameLongClick: ((Employee) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isSortMode: Boolean = false

        fun bind(employeeWithOvertime: EmployeeWithOvertime, sortMode: Boolean) {
            this.isSortMode = sortMode
            val employee = employeeWithOvertime.employee
            binding.textViewName.text = employee.name
            binding.textViewTwoMonthsAgo.text = "${employeeWithOvertime.overtimeTwoMonthsAgo}"
            binding.textViewLastMonth.text = "${employeeWithOvertime.overtimeLastMonth}"
            binding.textViewThisMonth.text = "${employeeWithOvertime.overtimeThisMonth}"
            binding.textViewAnnualTotal.text = "${employeeWithOvertime.annualTotal}"

            // 並び替えモード時の視覚的フィードバック
            if (sortMode) {
                binding.root.alpha = 0.8f
                (binding.root as? CardView)?.cardElevation = 8f
            } else {
                binding.root.alpha = 1.0f
                (binding.root as? CardView)?.cardElevation = 4f
            }

            // 残業時間に応じた背景色と文字色の設定
            val hours = employeeWithOvertime.overtimeThisMonth
            val backgroundColor = calculateBackgroundColor(hours)
            val textColor = calculateTextColor(hours)
            
            // 角丸の背景を作成
            val density = binding.root.resources.displayMetrics.density
            val cornerRadius = 8f * density // 8dpの角丸
            
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadius = cornerRadius
            shape.setColor(backgroundColor)
            
            binding.textViewThisMonth.background = shape
            binding.textViewThisMonth.setTextColor(textColor)

            binding.textViewThisMonth.setOnClickListener {
                onThisMonthClick(employee)
            }

            if (sortMode) {
                // 並び替えモード時は長押しで並び替えを開始
                binding.root.setOnLongClickListener {
                    // ItemTouchHelperが処理する
                    false
                }
                binding.textViewName.setOnLongClickListener(null)
            } else {
                binding.root.setOnLongClickListener(null)
                binding.textViewName.setOnLongClickListener {
                    onNameLongClick?.invoke(employee)
                    true
                }
            }
        }

        private fun calculateBackgroundColor(hours: Double): Int {
            val white = Color.WHITE
            val orange = Color.parseColor("#FFA500")
            val red = Color.RED
            val black = Color.BLACK

            val evaluator = ArgbEvaluator()

            return when {
                hours <= 0 -> white
                hours < 45 -> {
                    // 0-45: 白→オレンジ
                    val fraction = (hours / 45.0).toFloat()
                    evaluator.evaluate(fraction, white, orange) as Int
                }
                hours < 60 -> {
                    // 45-60: オレンジ→赤
                    val fraction = ((hours - 45) / (60 - 45)).toFloat()
                    evaluator.evaluate(fraction, orange, red) as Int
                }
                hours < 80 -> {
                    // 60-80: 赤→黒
                    val fraction = ((hours - 60) / (80 - 60)).toFloat()
                    evaluator.evaluate(fraction, red, black) as Int
                }
                else -> black
            }
        }

        private fun calculateTextColor(hours: Double): Int {
            val black = Color.BLACK
            val white = Color.WHITE

            return if (hours < 45) {
                // 45時間未満: 黒
                black
            } else {
                // 45時間以上: 白
                white
            }
        }

        fun onDragStart() {
            // ドラッグ開始時の視覚的効果
            binding.root.animate()
                .alpha(0.5f)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .start()
            (binding.root as? CardView)?.cardElevation = 16f
        }

        fun onDragEnd() {
            // ドラッグ終了時の視覚的効果を元に戻す
            binding.root.animate()
                .alpha(if (isSortMode) 0.8f else 1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start()
            (binding.root as? CardView)?.cardElevation = if (isSortMode) 8f else 4f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmployeeViewHolder(binding, onThisMonthClick, onNameLongClick)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(getItem(position), sortMode)
    }

    fun updateEmployeesWithOvertime(employeesWithOvertime: List<EmployeeWithOvertime>) {
        submitList(employeesWithOvertime)
    }

    fun setSortMode(enabled: Boolean) {
        sortMode = enabled
        notifyDataSetChanged()
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < 0 || fromPosition >= currentList.size ||
            toPosition < 0 || toPosition >= currentList.size) {
            return
        }
        val newList = currentList.toMutableList()
        val item = newList.removeAt(fromPosition)
        newList.add(toPosition, item)
        submitList(newList)
    }

    class EmployeeDiffCallback : DiffUtil.ItemCallback<EmployeeWithOvertime>() {
        override fun areItemsTheSame(oldItem: EmployeeWithOvertime, newItem: EmployeeWithOvertime): Boolean {
            return oldItem.employee.id == newItem.employee.id
        }

        override fun areContentsTheSame(oldItem: EmployeeWithOvertime, newItem: EmployeeWithOvertime): Boolean {
            return oldItem == newItem
        }
    }
}

