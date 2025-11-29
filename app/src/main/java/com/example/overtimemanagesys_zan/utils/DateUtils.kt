package com.example.overtimemanagesys_zan.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * 今日の日付をYYYY-MM-DD形式で取得
     */
    fun getTodayString(): String {
        return LocalDate.now().format(dateFormatter)
    }

    /**
     * 今月の期間（前月21日～今月20日）の開始日と終了日を取得
     */
    fun getCurrentMonthPeriod(): Pair<String, String> {
        val today = LocalDate.now()
        val startDate: LocalDate
        val endDate: LocalDate

        if (today.dayOfMonth >= 21) {
            // 今月21日以降の場合、今月21日～来月20日
            startDate = LocalDate.of(today.year, today.month, 21)
            endDate = if (today.monthValue == 12) {
                LocalDate.of(today.year + 1, 1, 20)
            } else {
                LocalDate.of(today.year, today.monthValue + 1, 20)
            }
        } else {
            // 今月20日以前の場合、前月21日～今月20日
            val lastMonth = if (today.monthValue == 1) {
                LocalDate.of(today.year - 1, 12, 21)
            } else {
                LocalDate.of(today.year, today.monthValue - 1, 21)
            }
            startDate = lastMonth
            endDate = LocalDate.of(today.year, today.month, 20)
        }

        return Pair(startDate.format(dateFormatter), endDate.format(dateFormatter))
    }

    /**
     * 先月の期間（先々月21日～先月20日）の開始日と終了日を取得
     */
    fun getLastMonthPeriod(): Pair<String, String> {
        val today = LocalDate.now()
        val startDate: LocalDate
        val endDate: LocalDate

        if (today.dayOfMonth >= 21) {
            // 今月21日以降の場合、先月21日～今月20日
            if (today.monthValue == 1) {
                startDate = LocalDate.of(today.year - 1, 12, 21)
            } else {
                startDate = LocalDate.of(today.year, today.monthValue - 1, 21)
            }
            endDate = LocalDate.of(today.year, today.month, 20)
        } else {
            // 今月20日以前の場合、先々月21日～先月20日
            val twoMonthsAgo = if (today.monthValue <= 2) {
                LocalDate.of(today.year - 1, 12 + today.monthValue - 1, 21)
            } else {
                LocalDate.of(today.year, today.monthValue - 2, 21)
            }
            startDate = twoMonthsAgo
            if (today.monthValue == 1) {
                endDate = LocalDate.of(today.year - 1, 12, 20)
            } else {
                endDate = LocalDate.of(today.year, today.monthValue - 1, 20)
            }
        }

        return Pair(startDate.format(dateFormatter), endDate.format(dateFormatter))
    }

    /**
     * 先々月の期間（先々々月21日～先々月20日）の開始日と終了日を取得
     */
    fun getTwoMonthsAgoPeriod(): Pair<String, String> {
        val today = LocalDate.now()
        val startDate: LocalDate
        val endDate: LocalDate

        if (today.dayOfMonth >= 21) {
            // 今月21日以降の場合、先々月21日～先月20日
            if (today.monthValue <= 2) {
                startDate = LocalDate.of(today.year - 1, 12 + today.monthValue - 1, 21)
            } else {
                startDate = LocalDate.of(today.year, today.monthValue - 2, 21)
            }
            if (today.monthValue == 1) {
                endDate = LocalDate.of(today.year - 1, 12, 20)
            } else {
                endDate = LocalDate.of(today.year, today.monthValue - 1, 20)
            }
        } else {
            // 今月20日以前の場合、先々々月21日～先々月20日
            val threeMonthsAgo = if (today.monthValue <= 3) {
                LocalDate.of(today.year - 1, 12 + today.monthValue - 2, 21)
            } else {
                LocalDate.of(today.year, today.monthValue - 3, 21)
            }
            startDate = threeMonthsAgo
            if (today.monthValue <= 2) {
                endDate = LocalDate.of(today.year - 1, 12 + today.monthValue - 1, 20)
            } else {
                endDate = LocalDate.of(today.year, today.monthValue - 2, 20)
            }
        }

        return Pair(startDate.format(dateFormatter), endDate.format(dateFormatter))
    }

    /**
     * 現在の年度期間（3/21～翌年3/20）の開始日と終了日を取得
     */
    fun getCurrentFiscalYearPeriod(): Pair<String, String> {
        val today = LocalDate.now()
        val startDate: LocalDate
        val endDate: LocalDate

        if (today.monthValue > 3 || (today.monthValue == 3 && today.dayOfMonth >= 21)) {
            // 3月21日以降の場合、今年の3月21日～来年の3月20日
            startDate = LocalDate.of(today.year, 3, 21)
            endDate = LocalDate.of(today.year + 1, 3, 20)
        } else {
            // 3月20日以前の場合、去年の3月21日～今年の3月20日
            startDate = LocalDate.of(today.year - 1, 3, 21)
            endDate = LocalDate.of(today.year, 3, 20)
        }

        return Pair(startDate.format(dateFormatter), endDate.format(dateFormatter))
    }
}

