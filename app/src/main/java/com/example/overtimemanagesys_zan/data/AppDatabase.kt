package com.example.overtimemanagesys_zan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Employee::class, OvertimeRecord::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun overtimeRecordDao(): OvertimeRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // バージョン2から3へのマイグレーション
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // displayOrderカラムを追加（デフォルト値は0）
                database.execSQL("ALTER TABLE employees ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                
                // 既存のデータに対してID順にdisplayOrderを設定
                // 表示中の従業員に対してID順に0から始まる連番を設定
                val visibleCursor = database.query(
                    "SELECT id FROM employees WHERE isVisible = 1 ORDER BY id ASC"
                )
                var order = 0
                while (visibleCursor.moveToNext()) {
                    val id = visibleCursor.getLong(0)
                    database.execSQL("UPDATE employees SET displayOrder = $order WHERE id = $id")
                    order++
                }
                visibleCursor.close()
                
                // 非表示の従業員に対しては、表示中の従業員の最大displayOrder + 1から始まる連番を設定
                val hiddenCursor = database.query(
                    "SELECT id FROM employees WHERE isVisible = 0 ORDER BY id ASC"
                )
                while (hiddenCursor.moveToNext()) {
                    val id = hiddenCursor.getLong(0)
                    database.execSQL("UPDATE employees SET displayOrder = $order WHERE id = $id")
                    order++
                }
                hiddenCursor.close()
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "employee_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                
                instance
            }
        }
    }
}

