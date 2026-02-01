package com.example.overtimemanagesys_zan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.overtimemanagesys_zan.adapter.CalendarAdapter
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.databinding.FragmentCalendarBinding
import com.example.overtimemanagesys_zan.utils.generateCalendarItems
import kotlinx.coroutines.launch
import java.time.YearMonth

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
    private var employeeId: Long = 0
    private var currentYearMonth: YearMonth = YearMonth.now()
    private lateinit var adapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())
        employeeId = arguments?.getLong("employeeId") ?: 0

        if (employeeId == 0L) {
            findNavController().popBackStack()
            return
        }

        adapter = CalendarAdapter { date ->
            // 新規に残業時間入力画面へ遷移
            val bundle = Bundle().apply {
                putLong("employeeId", employeeId)
                putString("selectedDate", date)
            }
            findNavController().navigate(R.id.action_CalendarFragment_to_OvertimeInputFragment, bundle)
        }

        binding.recyclerViewCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.recyclerViewCalendar.adapter = adapter

        binding.buttonPrevMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            loadCalendar()
        }

        binding.buttonNextMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            loadCalendar()
        }

        loadCalendar()
    }

    override fun onResume() {
        super.onResume()
        // メニューを更新（非表示にする）
        requireActivity().invalidateOptionsMenu()
    }

    private fun loadCalendar() {
        binding.textViewMonth.text = "${currentYearMonth.year}年${currentYearMonth.monthValue}月"

        lifecycleScope.launch {
            val calendarItems = generateCalendarItems(currentYearMonth) { date ->
                repository.getOvertimeRecord(employeeId, date)?.hours ?: 0.0
            }
            adapter.submitList(calendarItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

