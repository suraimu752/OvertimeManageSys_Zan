package com.example.overtimemanagesys_zan.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE isVisible = 1 ORDER BY displayOrder ASC, id ASC")
    fun getVisibleEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE isVisible = 0")
    fun getHiddenEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("UPDATE employees SET isVisible = :isVisible WHERE id = :id")
    suspend fun setEmployeeVisibility(id: Long, isVisible: Boolean)

    @Query("SELECT MAX(id) FROM employees")
    suspend fun getMaxId(): Long?

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Query("UPDATE employees SET displayOrder = :order WHERE id = :id")
    suspend fun updateDisplayOrder(id: Long, order: Int)

    @Query("UPDATE employees SET displayOrder = :order WHERE id IN (:ids)")
    suspend fun updateDisplayOrders(ids: List<Long>, order: Int)

    @Query("SELECT MAX(displayOrder) FROM employees WHERE isVisible = 1")
    suspend fun getMaxDisplayOrder(): Int?
}

