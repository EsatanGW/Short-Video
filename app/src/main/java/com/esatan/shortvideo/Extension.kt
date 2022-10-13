package com.esatan.shortvideo

import android.content.Context
import java.time.Year
import java.util.*

fun Long.parsedTime(context: Context): String {
    val result = System.currentTimeMillis() - this
    return when {
        result < 60 * 1000 -> context.getString(R.string.comment_time_just)
        result < 60 * 60 * 1000 -> "${result / (60 * 1000)}" + context.getString(R.string.comment_time_minute)
        result < 24 * 60 * 60 * 1000 -> "${result / (60 * 60 * 1000)}" + context.getString(R.string.comment_time_hour)
        else -> {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = this@parsedTime
            }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH).toTwoDigitString()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toTwoDigitString()
            if (year == Year.now().value) "$month-$day" else "$year-$month-$day"
        }
    }
}

fun Int.toTwoDigitString(): String = if (this < 10) "0$this" else this.toString()