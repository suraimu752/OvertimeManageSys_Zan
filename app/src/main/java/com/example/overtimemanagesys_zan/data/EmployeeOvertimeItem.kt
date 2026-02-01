package com.example.overtimemanagesys_zan.data

/**
 * 特定日付の従業員別残業時間表示用のモデル。
 */
data class EmployeeOvertimeItem(
    val employeeId: Long,
    val employeeName: String,
    val hours: Double
)
