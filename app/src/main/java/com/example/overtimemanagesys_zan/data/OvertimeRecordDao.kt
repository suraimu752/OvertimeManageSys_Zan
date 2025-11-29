package com.example.overtimemanagesys_zan.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeRecordDao {
    @Query("SELECT * FROM overtime_records WHERE employeeId = :employeeId AND date = :date")
    suspend fun getRecordByDate(employeeId: Long, date: String): OvertimeRecord?

    @Query("SELECT * FROM overtime_records WHERE employeeId = :employeeId AND date >= :startDate AND date <= :endDate")
    suspend fun getRecordsByDateRange(employeeId: Long, startDate: String, endDate: String): List<OvertimeRecord>

    @Query("SELECT SUM(hours) FROM overtime_records WHERE employeeId = :employeeId AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalHoursByDateRange(employeeId: Long, startDate: String, endDate: String): Double?

    @Query("SELECT * FROM overtime_records WHERE date = :date")
    suspend fun getRecordsByDate(date: String): List<OvertimeRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: OvertimeRecord)

    @Delete
    suspend fun deleteRecord(record: OvertimeRecord)
}

