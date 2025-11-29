package com.example.overtimemanagesys_zan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val overtimeTwoMonthsAgo: Int = 0, // 先々月残業時間（時間）
    val overtimeLastMonth: Int = 0,    // 先月残業時間（時間）
    val overtimeThisMonth: Int = 0,    // 今月残業時間（時間）
    val annualTotal: Int = 0,           // 年間合計（時間）
    val isVisible: Boolean = true,      // 表示/非表示フラグ
    val displayOrder: Int = 0           // 表示順序
)

