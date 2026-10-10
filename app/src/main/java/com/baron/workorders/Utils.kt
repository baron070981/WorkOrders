package com.baron.workorders

import android.content.Context
import android.text.Editable
import android.view.Gravity
import android.widget.EditText

fun formatData(value: Editable): String? {
    if (value.trim().isNotEmpty()){
        return value.toString()
    }
    return null
}

fun setTextPosition(et: EditText){
    val hint = et.hint
    if (et.text.isEmpty()){
        et.gravity = Gravity.CENTER
    }
    else{
        et.gravity = Gravity.TOP or Gravity.START
    }
    et.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            et.gravity = Gravity.TOP or Gravity.START
            et.hint = ""
        }
        else if (et.text.isNotEmpty()){
            et.gravity = Gravity.TOP or Gravity.START
        }
        else {
            et.gravity = Gravity.CENTER
            et.hint = hint
        }
    }
}

fun getStringOrNullFromEditText(et: EditText, capitalize: Boolean=false): String? {
    if (et.text.toString().trim().isEmpty()) return null
    if (capitalize) return et.text.toString().capitalizeWords().trim()
    return et.text.toString().trim()
}

fun getDoubleOrNull(v: String?): Double?{
    if (v == null || v.isEmpty()) return null
    return v.toDouble()
}

fun getDoubleOrNull(v: Editable?): Double?{
    if (v == null || v.isEmpty()) return null
    return v.toString().toDouble()
}

fun createAddress(street:String, house:String, city:String? = null,
                  apartment:String? = null, last_address:String? = null): String {
    var address = ""
    address += city ?: ""
    address += " ул.$street д.$house"
    if (apartment != null) address += " кв.$apartment"
    if (last_address != null) address += "\n$last_address"
    return address.trim()
}

fun String.capitalizeWords(): String {
    if (this.isBlank()) return this
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}












