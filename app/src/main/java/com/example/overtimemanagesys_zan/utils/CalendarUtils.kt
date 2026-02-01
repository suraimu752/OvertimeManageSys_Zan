package com.example.overtimemanagesys_zan.utils

import com.example.overtimemanagesys_zan.adapter.CalendarDateItem
import java.time.LocalDate
import java.time.YearMonth

/**
 * 指定年月のカレンダー表示用アイテムリストを生成する。
 * [getHoursForDate] で各日付の残業時間（または合計）を取得する。
 */
suspend fun generateCalendarItems(
    yearMonth: YearMonth,
    getHoursForDate: suspend (String) -> Double
): List<CalendarDateItem> {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()
    val startOfWeek = firstDay.dayOfWeek.value % 7 // 日曜日を0にする
    val dateFormatter = DateUtils.getDateFormatter()
    val today = LocalDate.now()
    val todayString = today.format(dateFormatter)

    val items = mutableListOf<CalendarDateItem>()

    // 前月の日付（空白）
    for (i in 0 until startOfWeek) {
        val date = firstDay.minusDays((startOfWeek - i).toLong())
        val dateString = date.format(dateFormatter)
        items.add(
            CalendarDateItem(
                date = dateString,
                day = date.dayOfMonth,
                hours = 0.0,
                isCurrentMonth = false,
                isToday = dateString == todayString,
                isFuture = date.isAfter(today)
            )
        )
    }

    // 今月の日付
    var currentDate = firstDay
    while (!currentDate.isAfter(lastDay)) {
        val dateString = currentDate.format(dateFormatter)
        val hours = getHoursForDate(dateString)
        items.add(
            CalendarDateItem(
                date = dateString,
                day = currentDate.dayOfMonth,
                hours = hours,
                isCurrentMonth = true,
                isToday = dateString == todayString,
                isFuture = currentDate.isAfter(today)
            )
        )
        currentDate = currentDate.plusDays(1)
    }

    // 次月の日付（7列グリッドを埋める）
    val remainingDays = (7 - (items.size % 7)) % 7
    for (i in 1..remainingDays) {
        val date = lastDay.plusDays(i.toLong())
        val dateString = date.format(dateFormatter)
        items.add(
            CalendarDateItem(
                date = dateString,
                day = date.dayOfMonth,
                hours = 0.0,
                isCurrentMonth = false,
                isToday = dateString == todayString,
                isFuture = date.isAfter(today)
            )
        )
    }

    return items
}
