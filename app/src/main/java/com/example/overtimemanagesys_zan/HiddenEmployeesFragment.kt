package com.example.overtimemanagesys_zan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.overtimemanagesys_zan.adapter.EmployeeAdapter
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.data.EmployeeWithOvertime
import com.example.overtimemanagesys_zan.databinding.FragmentHiddenEmployeesBinding
import com.example.overtimemanagesys_zan.utils.DateUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HiddenEmployeesFragment : Fragment() {

    private var _binding: FragmentHiddenEmployeesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
    private lateinit var adapter: EmployeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        _binding = FragmentHiddenEmployeesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())

        adapter = EmployeeAdapter(
            onThisMonthClick = { employee ->
                // 今月の残業時間をタップした時の処理（非表示人員では使用しない）
            },
            onNameLongClick = { employee ->
                // 長押しで再表示
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.setEmployeeVisibility(employee.id, true)
                }
            }
        )

        binding.recyclerViewHiddenEmployees.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHiddenEmployees.adapter = adapter

        // 非表示の従業員リストを監視
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getHiddenEmployees().collectLatest { employees ->
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
        // メニューを更新（非表示にする）
        requireActivity().invalidateOptionsMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

