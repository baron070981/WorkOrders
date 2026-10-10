package com.baron.workorders

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.baron.workorders.databinding.ActivityInsertDataBinding
//import kotlin.getValue


class InsertDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsertDataBinding
    private lateinit var dateCreate: EditText
    private lateinit var toDate: EditText
    private lateinit var city: EditText
    private lateinit var street: EditText
    private lateinit var house: EditText
    private lateinit var apartment: EditText
    private lateinit var address: EditText
    private lateinit var clientName: EditText
    private lateinit var clientPhone: EditText
    private lateinit var task: EditText
    private lateinit var note: EditText
    private lateinit var btnsave: Button
    private val taskviewmodel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as DataBaseApp).repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInsertDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initEditText()
        btnsave = binding.btnInsertSave.apply {
            setOnClickListener {
                val task = getDataFromEditText()
                if (task != null){
                    taskviewmodel.addTask(task)
                    NotCompletedViewWidget.forceUpdate(this@InsertDataActivity)
                    finish()
                }
            }
        }



    }


    private fun setDate(et: EditText){
        val hint = et.hint
        et.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showDatePicker(this@InsertDataActivity, et)
            }
        }
    }


    private fun initEditText(){
        dateCreate = binding.etInsertDateCreate.apply {
            setText(getCurrentDateString())
            setDate(this)
        }
        toDate = binding.etInsertTodate.apply {
            setTextPosition(this)
            setDate(this)
        }
        city = binding.etInsertCity.apply { setTextPosition(this) }
        street = binding.etInsertStreet.apply { setTextPosition(this) }
        house = binding.etInsertHouse.apply { setTextPosition(this) }
        apartment = binding.etInsertApartment.apply { setTextPosition(this) }
        address = binding.etInsertAddress.apply { setTextPosition(this) }
        clientName = binding.etInsertClientName.apply { setTextPosition(this) }
        clientPhone = binding.etInsertClientPhone.apply { setTextPosition(this) }
        task = binding.etInsertTask.apply {  setTextPosition(this) }
        note = binding.etInsertNote.apply { setTextPosition(this) }
    }

    private fun getDataFromEditText(): Task?{
        if (isValidField(dateCreate) { s -> s.isEmpty() }){
            Toast.makeText(this, "Поле \"дата создания\" не должно быть пустым", Toast.LENGTH_SHORT).show()
            return null
        }
        if (isValidField(street) { s -> s.isEmpty() }){
            Toast.makeText(this, "Поле \"улица\" не должно быть пустым", Toast.LENGTH_SHORT).show()
            return null
        }
        if (isValidField(house) { s -> s.isEmpty() }){
            Toast.makeText(this, "Поле \"дом\" не должно быть пустым", Toast.LENGTH_SHORT).show()
            return null
        }
        if (isValidField(task) { s -> s.isEmpty() }){
            Toast.makeText(this, "Поле \"заявка\" не должно быть пустым", Toast.LENGTH_SHORT).show()
            return null
        }

        val datecreate = dateCreate.text.toString().trim()
        val todate = getStringOrNullFromEditText(toDate)
        val _city = getStringOrNullFromEditText(city, true)
        val _street = street.text.toString().capitalizeWords().trim()
        val _house = house.text.toString().trim()
        val _apartment = getStringOrNullFromEditText(apartment)
        val _address = getStringOrNullFromEditText(address)
        val name = getStringOrNullFromEditText(clientName, true)
        val phone = getStringOrNullFromEditText(clientPhone)
        val _task = task.text.toString().trim()
        val _note = getStringOrNullFromEditText(note)

        return Task(
            date_receipt = datecreate, date_scheduled = todate, date_complete = null, city=_city, street=_street,
            house = _house, apartment = _apartment, remainder_address = _address, name = name, phone = phone,
            task = _task, note = _note, completed = null, materials = null, pay = null, isComplete = false
        )
    }
}





