package com.example.overtimemanagesys_zan.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val employeeDao = database.employeeDao()
    private val overtimeRecordDao = database.overtimeRecordDao()

    fun getAllEmployees(): Flow<List<Employee>> {
        return employeeDao.getAllEmployees()
    }

    fun getVisibleEmployees(): Flow<List<Employee>> {
        return employeeDao.getVisibleEmployees()
    }

    fun getHiddenEmployees(): Flow<List<Employee>> {
        return employeeDao.getHiddenEmployees()
    }

    suspend fun getEmployeeById(id: Long): Employee? {
        return employeeDao.getEmployeeById(id)
    }

    suspend fun addEmployee(employee: Employee): Long {
        return employeeDao.insertEmployee(employee)
    }

    suspend fun updateEmployee(employee: Employee) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        employeeDao.deleteEmployee(employee)
    }

    suspend fun setEmployeeVisibility(employeeId: Long, isVisible: Boolean) {
        employeeDao.setEmployeeVisibility(employeeId, isVisible)
    }

    suspend fun getNextId(): Long {
        val maxId = employeeDao.getMaxId()
        return (maxId ?: 0L) + 1L
    }

    // 残業時間記録関連
    suspend fun saveOvertimeRecord(employeeId: Long, date: String, hours: Double) {
        val record = OvertimeRecord(0, employeeId, date, hours)
        overtimeRecordDao.insertOrUpdateRecord(record)
    }

    suspend fun getOvertimeRecord(employeeId: Long, date: String): OvertimeRecord? {
        return overtimeRecordDao.getRecordByDate(employeeId, date)
    }

    suspend fun getTotalHoursByDateRange(employeeId: Long, startDate: String, endDate: String): Double {
        return overtimeRecordDao.getTotalHoursByDateRange(employeeId, startDate, endDate) ?: 0.0
    }

    suspend fun getOvertimeRecordsByDate(date: String): List<OvertimeRecord> {
        return overtimeRecordDao.getRecordsByDate(date)
    }

    suspend fun updateEmployeeDisplayOrder(employeeId: Long, order: Int) {
        employeeDao.updateDisplayOrder(employeeId, order)
    }

    suspend fun updateEmployeeDisplayOrders(updates: Map<Long, Int>) {
        updates.forEach { (id, order) ->
            employeeDao.updateDisplayOrder(id, order)
        }
    }

    suspend fun getMaxDisplayOrder(): Int {
        return employeeDao.getMaxDisplayOrder() ?: -1
    }
}
