package com.example.overtimemanagesys_zan.data

data class EmployeeWithOvertime(
    val employee: Employee,
    val overtimeTwoMonthsAgo: Double,
    val overtimeLastMonth: Double,
    val overtimeThisMonth: Double,
    val annualTotal: Double,
    val monthsOver45Hours: Int = 0
)

