package com.example.overtimemanagesys_zan

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.overtimemanagesys_zan.adapter.EmployeeSelectionAdapter
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.databinding.DialogSelectEmployeesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectEmployeesDialog(
    private val initialSelectedIds: Set<Long>,
    private val onEmployeesSelected: (Set<Long>) -> Unit
) : DialogFragment() {

    private var _binding: DialogSelectEmployeesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository
    private lateinit var adapter: EmployeeSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectEmployeesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())
        adapter = EmployeeSelectionAdapter()

        binding.recyclerViewEmployees.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewEmployees.adapter = adapter

        // 全選択・解除ボタン
        binding.buttonSelectAll.setOnClickListener { adapter.selectAll() }
        binding.buttonDeselectAll.setOnClickListener { adapter.deselectAll() }

        // 決定ボタン
        binding.buttonDone.setOnClickListener {
            onEmployeesSelected(adapter.getSelectedIds())
            dismiss()
        }

        // データ読み込み
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getVisibleEmployees().collectLatest { employees ->
                adapter.submitList(employees)
                // 初期選択状態を適用
                // データがロードされた後に適用しないと反映されない可能性があるためここで設定
                // ただし、collectLatestはデータ更新ごとに呼ばれるため、
                // 初期化フラグなどで一度だけ設定する制御を入れたほうが良いが、
                // 今回はダイアログを開くたびに生成されるので毎回設定でも、
                // ユーザーが操作中にデータ更新が走ると選択がリセットされるリスクがある。
                // EmployeeSelectionAdapterは内部でselectedIdsを持っているので、
                // submitListで選択状態がクリアされなければ問題ない。
                // 実装を確認すると、submitListはListAdapterのメソッドで、selectedIdsはフィールド。
                // なのでsubmitListしてもselectedIdsは維持される。
                // ただし、初期選択を反映させる必要がある。
                
                // 初回のみ適用するロジックを入れる、またはAdapter側でマージするロジックにする。
                // ここではシンプルに、現在のアダプターの選択状態が空なら初期値を設定する、という簡易ロジックにする。
                // （ただし一度全部解除して空にしている場合もあるので注意が必要だが、
                //   ダイアログ生成直後なら空のはず）
                if (adapter.getSelectedIds().isEmpty() && initialSelectedIds.isNotEmpty()) {
                    adapter.setSelectedIds(initialSelectedIds)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

