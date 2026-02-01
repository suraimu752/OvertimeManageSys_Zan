package com.example.overtimemanagesys_zan.data

import android.content.Context
import com.example.overtimemanagesys_zan.utils.DateUtils
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

    /**
     * 従業員リストに対して期間別残業時間・年度合計・45時間超え月数を計算し、
     * [EmployeeWithOvertime] のリストを返す。
     */
    suspend fun computeEmployeesWithOvertime(employees: List<Employee>): List<EmployeeWithOvertime> {
        val (twoMonthsAgoStart, twoMonthsAgoEnd) = DateUtils.getTwoMonthsAgoPeriod()
        val (lastMonthStart, lastMonthEnd) = DateUtils.getLastMonthPeriod()
        val (thisMonthStart, thisMonthEnd) = DateUtils.getCurrentMonthPeriod()
        val (fiscalYearStart, fiscalYearEnd) = DateUtils.getCurrentFiscalYearPeriod()
        val past12MonthsPeriods = DateUtils.getPast12MonthsPeriods()

        return employees.map { employee ->
            val twoMonthsAgo = getTotalHoursByDateRange(employee.id, twoMonthsAgoStart, twoMonthsAgoEnd)
            val lastMonth = getTotalHoursByDateRange(employee.id, lastMonthStart, lastMonthEnd)
            val thisMonth = getTotalHoursByDateRange(employee.id, thisMonthStart, thisMonthEnd)
            val annualTotal = getTotalHoursByDateRange(employee.id, fiscalYearStart, fiscalYearEnd)
            val monthsOver45Hours = past12MonthsPeriods.count { (startDate, endDate) ->
                getTotalHoursByDateRange(employee.id, startDate, endDate) > 45.0
            }
            EmployeeWithOvertime(
                employee = employee,
                overtimeTwoMonthsAgo = twoMonthsAgo,
                overtimeLastMonth = lastMonth,
                overtimeThisMonth = thisMonth,
                annualTotal = annualTotal,
                monthsOver45Hours = monthsOver45Hours
            )
        }
    }
}
