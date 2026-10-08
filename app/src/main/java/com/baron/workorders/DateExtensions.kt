package com.baron.workorders

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.time.LocalDate


fun getCurrentDateString(): String {
    val (day, month, year) = getDateValues()
    val month_s = month.toString().padStart(2, '0')
    val day_s = day.toString().padStart(2, '0')
    return "$day_s.$month_s.$year"
}

fun getDateValues(): Triple<Int, Int, Int> {
    /*
    * return -> day: Int, month: Int, year: Int
    */
    val date = LocalDate.now()
    val year = date.year
    val month = date.monthValue
    val day = date.dayOfMonth
    return Triple(day, month, year)
}

fun showDatePicker(context: Context, et: EditText) {
    val (day, month, year) = getDateValues()
    DatePickerDialog(context, { _, y, m, d ->
        val _d = d.toString().padStart(2, '0')
        val _m = (m+1).toString().padStart(2, '0')
        et.setText("$_d.$_m.$y")
    }, year, month-1, day).show()
}
















