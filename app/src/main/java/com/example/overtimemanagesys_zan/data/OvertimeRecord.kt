package com.example.overtimemanagesys_zan.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "overtime_records",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId", "date"], unique = true)]
)
data class OvertimeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val date: String, // YYYY-MM-DD形式
    val hours: Double // 残業時間（0.5h単位）
)

