package com.baron.workorders

import android.view.View
import android.widget.AdapterView

class SpinnerListener (
    private val onItemChosen: (Int) -> Unit // Передаем индекс выбранного элемента
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        onItemChosen(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Обязательный метод интерфейса, оставляем пустым
    }
}














