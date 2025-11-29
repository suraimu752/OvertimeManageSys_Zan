package com.example.overtimemanagesys_zan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.overtimemanagesys_zan.adapter.DateOvertimeListAdapter
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.databinding.FragmentDateOvertimeListBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class EmployeeOvertimeItem(
    val employeeId: Long,
    val employeeName: String,
    val hours: Double
)

class DateOvertimeListFragment : Fragment() {

    private var _binding: FragmentDateOvertimeListBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
    private var selectedDate: String = ""
    private lateinit var adapter: DateOvertimeListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        _binding = FragmentDateOvertimeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())
        selectedDate = arguments?.getString("selectedDate") ?: ""

        if (selectedDate.isEmpty()) {
            findNavController().popBackStack()
            return
        }

        adapter = DateOvertimeListAdapter { employeeId ->
            // 従業員をタップした時に残業時間入力画面へ遷移
            val bundle = Bundle().apply {
                putLong("employeeId", employeeId)
                putString("selectedDate", selectedDate)
            }
            findNavController().navigate(R.id.action_DateOvertimeListFragment_to_OvertimeInputFragment, bundle)
        }

        binding.recyclerViewEmployees.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEmployees.adapter = adapter

        // 日付表示を更新
        try {
            val date = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            binding.textViewDate.text = "日付: ${date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}"
        } catch (e: Exception) {
            binding.textViewDate.text = "日付: $selectedDate"
        }

        loadOvertimeList()
    }

    override fun onResume() {
        super.onResume()
        // メニューを更新（非表示にする）
        requireActivity().invalidateOptionsMenu()
    }

    private fun loadOvertimeList() {
        lifecycleScope.launch {
            // 選択された日付の残業時間記録を取得
            val records = repository.getOvertimeRecordsByDate(selectedDate)
            
            // 従業員情報を取得してリストを作成
            val items = records.mapNotNull { record ->
                val employee = repository.getEmployeeById(record.employeeId)
                employee?.let {
                    EmployeeOvertimeItem(
                        employeeId = it.id,
                        employeeName = it.name,
                        hours = record.hours
                    )
                }
            }
            
            adapter.submitList(items)
            
            // データがない場合のメッセージ
            if (items.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
            } else {
                binding.textViewEmpty.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

