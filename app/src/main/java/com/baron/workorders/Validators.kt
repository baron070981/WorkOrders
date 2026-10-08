package com.baron.workorders

import android.widget.EditText



fun isValidField(et: EditText, validator: (String)->Boolean): Boolean {
    return validator(et.text.toString())
}









