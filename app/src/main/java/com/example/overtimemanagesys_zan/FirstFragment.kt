package com.example.overtimemanagesys_zan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.overtimemanagesys_zan.adapter.EmployeeAdapter
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.data.EmployeeWithOvertime
import com.example.overtimemanagesys_zan.databinding.FragmentFirstBinding
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.overtimemanagesys_zan.utils.DateUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 残業管理リスト表示画面
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
    private lateinit var adapter: EmployeeAdapter
    private var isSortMode: Boolean = false
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())

        adapter = EmployeeAdapter(
            onThisMonthClick = { employee ->
                // 今月の残業時間をタップした時の処理
                val bundle = Bundle().apply {
                    putLong("employeeId", employee.id)
                }
                findNavController().navigate(R.id.action_FirstFragment_to_OvertimeInputFragment, bundle)
            },
            onNameLongClick = { employee ->
                // 名前を長押しした時の処理（編集・非表示ダイアログ）
                val dialog = EmployeeEditDialog(employee, repository) {
                    // 更新後の処理（Flowが自動的に更新される）
                }
                dialog.show(parentFragmentManager, "EmployeeEditDialog")
            }
        )

        binding.recyclerViewEmployees.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEmployees.adapter = adapter

        // 表示中の従業員リストを監視
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getVisibleEmployees().collectLatest { employees ->
                // 各従業員の期間ごとの残業時間を計算
                val employeesWithOvertime = employees.map { employee ->
                    val (twoMonthsAgoStart, twoMonthsAgoEnd) = DateUtils.getTwoMonthsAgoPeriod()
                    val (lastMonthStart, lastMonthEnd) = DateUtils.getLastMonthPeriod()
                    val (thisMonthStart, thisMonthEnd) = DateUtils.getCurrentMonthPeriod()
                    val (fiscalYearStart, fiscalYearEnd) = DateUtils.getCurrentFiscalYearPeriod()

                    val twoMonthsAgo = repository.getTotalHoursByDateRange(
                        employee.id, twoMonthsAgoStart, twoMonthsAgoEnd
                    )
                    val lastMonth = repository.getTotalHoursByDateRange(
                        employee.id, lastMonthStart, lastMonthEnd
                    )
                    val thisMonth = repository.getTotalHoursByDateRange(
                        employee.id, thisMonthStart, thisMonthEnd
                    )
                    // 年度期間（3/21～翌年3/20）の合計を計算
                    val annualTotal = repository.getTotalHoursByDateRange(
                        employee.id, fiscalYearStart, fiscalYearEnd
                    )

                    EmployeeWithOvertime(
                        employee = employee,
                        overtimeTwoMonthsAgo = twoMonthsAgo,
                        overtimeLastMonth = lastMonth,
                        overtimeThisMonth = thisMonth,
                        annualTotal = annualTotal
                    )
                }
                adapter.updateEmployeesWithOvertime(employeesWithOvertime)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // メニューを更新（表示する）
        requireActivity().invalidateOptionsMenu()
    }

    fun isSortMode(): Boolean = isSortMode

    fun toggleSortMode() {
        isSortMode = true
        adapter.setSortMode(true)
        setupItemTouchHelper()
        requireActivity().invalidateOptionsMenu()
    }

    private fun setupItemTouchHelper() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.moveItem(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                direction: Int
            ) {
                // スワイプは使用しない
            }

            override fun isLongPressDragEnabled(): Boolean {
                return isSortMode
            }

            override fun onSelectedChanged(
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?,
                actionState: Int
            ) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                    // ドラッグ開始時
                    (viewHolder as? EmployeeAdapter.EmployeeViewHolder)?.onDragStart()
                }
            }

            override fun clearView(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                // ドラッグ終了時
                (viewHolder as? EmployeeAdapter.EmployeeViewHolder)?.onDragEnd()
            }
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.recyclerViewEmployees)
    }

    fun saveSortOrder() {
        lifecycleScope.launch {
            val currentList = adapter.currentList
            val updates = currentList.mapIndexed { index, item ->
                item.employee.id to index
            }.toMap()
            repository.updateEmployeeDisplayOrders(updates)
            isSortMode = false
            adapter.setSortMode(false)
            itemTouchHelper?.attachToRecyclerView(null)
            itemTouchHelper = null
            requireActivity().invalidateOptionsMenu()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
