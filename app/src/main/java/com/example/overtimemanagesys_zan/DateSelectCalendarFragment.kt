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
import com.example.overtimemanagesys_zan.adapter.CalendarDateItem
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.databinding.FragmentCalendarBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth

class DateSelectCalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
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

        adapter = CalendarAdapter { date ->
            // 日付選択時に残業時間入力済み人員リスト画面へ遷移
            val bundle = Bundle().apply {
                putString("selectedDate", date)
            }
            findNavController().navigate(R.id.action_DateSelectCalendarFragment_to_DateOvertimeListFragment, bundle)
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
            val calendarItems = generateCalendarItems(currentYearMonth)
            adapter.submitList(calendarItems)
        }
    }

    private suspend fun generateCalendarItems(yearMonth: YearMonth): List<CalendarDateItem> {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        val startOfWeek = firstDay.dayOfWeek.value % 7 // 日曜日を0にする
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val todayString = today.format(dateFormatter)

        val items = mutableListOf<CalendarDateItem>()

        // 前月の日付（空白）
        for (i in 0 until startOfWeek) {
            val date = firstDay.minusDays((startOfWeek - i).toLong())
            val dateString = date.format(dateFormatter)
            val isToday = dateString == todayString
            val isFuture = date.isAfter(today)
            items.add(CalendarDateItem(
                date = dateString,
                day = date.dayOfMonth,
                hours = 0.0,
                isCurrentMonth = false,
                isToday = isToday,
                isFuture = isFuture
            ))
        }

        // 今月の日付
        var currentDate = firstDay
        while (!currentDate.isAfter(lastDay)) {
            val dateString = currentDate.format(dateFormatter)
            // 特定日付の全従業員の残業時間を取得して合計
            val records = repository.getOvertimeRecordsByDate(dateString)
            val totalHours = records.sumOf { it.hours }
            val isToday = dateString == todayString
            val isFuture = currentDate.isAfter(today)
            items.add(CalendarDateItem(
                date = dateString,
                day = currentDate.dayOfMonth,
                hours = totalHours,
                isCurrentMonth = true,
                isToday = isToday,
                isFuture = isFuture
            ))
            currentDate = currentDate.plusDays(1)
        }

        // 次月の日付（空白） - 7列のグリッドを埋める
        val remainingDays = (7 - (items.size % 7)) % 7
        for (i in 1..remainingDays) {
            val date = lastDay.plusDays(i.toLong())
            val dateString = date.format(dateFormatter)
            val isToday = dateString == todayString
            val isFuture = date.isAfter(today)
            items.add(CalendarDateItem(
                date = dateString,
                day = date.dayOfMonth,
                hours = 0.0,
                isCurrentMonth = false,
                isToday = isToday,
                isFuture = isFuture
            ))
        }

        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

