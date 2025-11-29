package com.example.overtimemanagesys_zan

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.overtimemanagesys_zan.data.Employee
import com.example.overtimemanagesys_zan.data.EmployeeRepository
import kotlinx.coroutines.launch

class EmployeeEditDialog(
    private val employee: Employee,
    private val repository: EmployeeRepository,
    private val onEmployeeUpdated: () -> Unit
) : DialogFragment() {

    private var dialogContext: Context? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogContext = requireContext()
        val context = dialogContext ?: return super.onCreateDialog(savedInstanceState)
        
        val editText = EditText(context).apply {
            setText(employee.name)
            hint = "名前"
        }

        return AlertDialog.Builder(context)
            .setTitle("人員編集")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val newName = editText.text.toString().trim()
                val ctx = dialogContext
                if (newName.isEmpty()) {
                    ctx?.let { Toast.makeText(it, "名前を入力してください", Toast.LENGTH_SHORT).show() }
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val updatedEmployee = employee.copy(name = newName)
                    repository.updateEmployee(updatedEmployee)
                    ctx?.let { Toast.makeText(it, "更新しました", Toast.LENGTH_SHORT).show() }
                    onEmployeeUpdated()
                }
            }
            .setNegativeButton("キャンセル", null)
            .setNeutralButton("非表示") { _, _ ->
                val ctx = dialogContext
                lifecycleScope.launch {
                    repository.setEmployeeVisibility(employee.id, false)
                    ctx?.let { Toast.makeText(it, "非表示にしました", Toast.LENGTH_SHORT).show() }
                    onEmployeeUpdated()
                }
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogContext = null
    }
}

