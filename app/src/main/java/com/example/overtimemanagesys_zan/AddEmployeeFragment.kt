package com.example.overtimemanagesys_zan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.overtimemanagesys_zan.data.Employee
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import com.example.overtimemanagesys_zan.databinding.FragmentAddEmployeeBinding
import kotlinx.coroutines.launch

class AddEmployeeFragment : Fragment() {

    private var _binding: FragmentAddEmployeeBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: EmployeeRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(false)
        _binding = FragmentAddEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = EmployeeRepository(requireContext())

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "名前を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val maxOrder = repository.getMaxDisplayOrder()
                val newEmployee = Employee(
                    id = 0, // Roomが自動生成
                    name = name,
                    overtimeTwoMonthsAgo = 0,
                    overtimeLastMonth = 0,
                    overtimeThisMonth = 0,
                    annualTotal = 0,
                    isVisible = true,
                    displayOrder = maxOrder + 1
                )

                repository.addEmployee(newEmployee)
                Toast.makeText(requireContext(), "人員を追加しました", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
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

