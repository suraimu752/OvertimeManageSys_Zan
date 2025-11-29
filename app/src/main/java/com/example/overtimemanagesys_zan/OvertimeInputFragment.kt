package com.example.overtimemanagesys_zan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.databinding.FragmentOvertimeInputBinding
import com.example.overtimemanagesys_zan.utils.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OvertimeInputFragment : Fragment() {

    private var _binding: FragmentOvertimeInputBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
    private var employeeId: Long = 0
    private var selectedDate: String = ""
    private var tempHours: Double = 0.0
    private var isMinusMode: Boolean = false
    private var defaultButtonTintList: android.content.res.ColorStateList? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        _binding = FragmentOvertimeInputBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())
        employeeId = arguments?.getLong("employeeId") ?: 0
        val dateArg = arguments?.getString("selectedDate")
        selectedDate = if (dateArg.isNullOrEmpty()) {
            DateUtils.getTodayString()
        } else {
            dateArg
        }

        if (employeeId == 0L) {
            Toast.makeText(requireContext(), "従業員情報が取得できませんでした", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        try {
            setupUI()
            loadEmployeeInfo()
            loadCurrentMonthTotal()
            loadSelectedDateRecord()
            
            // カレンダーから日付が選択された時のリスナー
            setFragmentResultListener("selectedDate") { requestKey, bundle ->
                val date = bundle.getString("date")
                if (!date.isNullOrEmpty()) {
                    updateSelectedDate(date)
                }
            }
            
            // カレンダーボタンの処理
            binding.buttonCalendar.setOnClickListener {
                val bundle = Bundle().apply {
                    putLong("employeeId", employeeId)
                }
                // カレンダーに遷移（カレンダーをスタックに追加）
                findNavController().navigate(R.id.action_OvertimeInputFragment_to_CalendarFragment, bundle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "エラーが発生しました: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // カレンダーから戻ってきた時に確実にリスナーを設定
        setFragmentResultListener("selectedDate") { requestKey, bundle ->
            val date = bundle.getString("date")
            if (!date.isNullOrEmpty()) {
                updateSelectedDate(date)
            }
        }
        // メニューを更新（非表示にする）
        requireActivity().invalidateOptionsMenu()
    }

    private fun setupUI() {
        try {
            val date = LocalDate.parse(selectedDate, dateFormatter)
            updateDateDisplay(date)
        } catch (e: Exception) {
            // 日付のパースに失敗した場合は今日の日付を使用
            selectedDate = DateUtils.getTodayString()
            val date = LocalDate.parse(selectedDate, dateFormatter)
            updateDateDisplay(date)
        }

        // 時間ボタンの設定
        binding.button05h.setOnClickListener { addHours(0.5) }
        binding.button1h.setOnClickListener { addHours(1.0) }
        binding.button2h.setOnClickListener { addHours(2.0) }
        binding.button4h.setOnClickListener { addHours(4.0) }

        // マイナスボタンの設定
        defaultButtonTintList = binding.buttonMinus.backgroundTintList
        binding.buttonMinus.setOnClickListener {
            isMinusMode = !isMinusMode
            updateMinusButtonState()
        }
        updateMinusButtonState()

        // 明日ボタンの設定
        binding.buttonTomorrow.setOnClickListener {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowString = tomorrow.format(dateFormatter)
            updateSelectedDate(tomorrowString)
        }

        // クリアボタン
        binding.buttonClear.setOnClickListener {
            tempHours = 0.0
            updateTempInputDisplay()
        }

        // 確定ボタン
        binding.buttonConfirm.setOnClickListener {
            saveOvertime()
        }
    }

    private fun addHours(hours: Double) {
        if (isMinusMode) {
            tempHours -= hours
            if (tempHours < 0) tempHours = 0.0
            // マイナスモードを維持（トグルしない）
        } else {
            tempHours += hours
        }
        updateTempInputDisplay()
    }

    private fun updateMinusButtonState() {
        if (isMinusMode) {
            binding.buttonMinus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
        } else {
            binding.buttonMinus.backgroundTintList = defaultButtonTintList
        }
    }

    private fun updateTempInputDisplay() {
        binding.textViewTempInput.text = "仮入力: ${tempHours}h"
    }

    private fun loadEmployeeInfo() {
        lifecycleScope.launch {
            val employee = repository.getEmployeeById(employeeId)
            employee?.let {
                binding.textViewEmployeeName.text = it.name
            }
        }
    }

    private fun loadCurrentMonthTotal() {
        lifecycleScope.launch {
            val (startDate, endDate) = DateUtils.getCurrentMonthPeriod()
            val total = repository.getTotalHoursByDateRange(employeeId, startDate, endDate)
            binding.textViewCurrentMonthTotal.text = "今月の合計: ${total}h"
        }
    }

    private fun loadSelectedDateRecord() {
        lifecycleScope.launch {
            // 現在選択されている日付のレコードを読み込む
            val currentSelectedDate = selectedDate
            val record = repository.getOvertimeRecord(employeeId, currentSelectedDate)
            // 読み込み中に日付が変更されていないか確認
            if (currentSelectedDate == selectedDate) {
                record?.let {
                    tempHours = it.hours
                    updateTempInputDisplay()
                } ?: run {
                    // レコードがない場合は0のまま
                    tempHours = 0.0
                    updateTempInputDisplay()
                }
            }
        }
    }

    private fun saveOvertime() {
        lifecycleScope.launch {
            repository.saveOvertimeRecord(employeeId, selectedDate, tempHours)
            Toast.makeText(requireContext(), "残業時間を保存しました", Toast.LENGTH_SHORT).show()
            
            // 合計を更新
            loadCurrentMonthTotal()
            
            // 仮入力をリセット
            tempHours = 0.0
            updateTempInputDisplay()
            
            // 前の画面に戻る（一覧またはカレンダー）
            findNavController().popBackStack()
        }
    }

    fun updateSelectedDate(date: String) {
        if (_binding == null) {
            return
        }
        try {
            selectedDate = date
            val dateObj = LocalDate.parse(selectedDate, dateFormatter)
            updateDateDisplay(dateObj)
            // まず仮入力を0にリセットして表示を更新
            tempHours = 0.0
            updateTempInputDisplay()
            // その後、選択された日付のレコードを読み込む
            loadSelectedDateRecord()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isTomorrow(date: LocalDate): Boolean {
        val tomorrow = LocalDate.now().plusDays(1)
        return date == tomorrow
    }

    private fun updateDateDisplay(date: LocalDate) {
        val dateText = "日付: ${date.format(dateFormatter)}"
        binding.textViewDate.text = dateText
        
        if (isTomorrow(date)) {
            // 明日の場合はハイライト表示
            binding.textViewDate.setTextColor(android.graphics.Color.parseColor("#FF6B35")) // オレンジ色
            binding.textViewDate.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.textViewDate.textSize = 18f
        } else {
            // 通常の日付の場合はデフォルト表示
            binding.textViewDate.setTextColor(android.graphics.Color.parseColor("#000000")) // 黒色
            binding.textViewDate.setTypeface(null, android.graphics.Typeface.NORMAL)
            binding.textViewDate.textSize = 16f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
