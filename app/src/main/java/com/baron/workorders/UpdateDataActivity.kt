package com.baron.workorders

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.baron.workorders.databinding.ActivityUpdateDataBinding
import kotlinx.coroutines.launch
import kotlin.getValue


const val KEY_RETURN_FROM_UPDATE = "from_update"
const val KEY_RETURN_FROM_UPDATE_I = "from_update2"

class UpdateDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateDataBinding
    private lateinit var dateCreate: EditText
    private lateinit var dateComplete: EditText
    private lateinit var toDate: EditText
    private lateinit var city: EditText
    private lateinit var street: EditText
    private lateinit var house: EditText
    private lateinit var apartment: EditText
    private lateinit var address: EditText
    private lateinit var clientName: EditText
    private lateinit var clientPhone: EditText
    private lateinit var task: EditText
    private lateinit var completedWorks: EditText
    private lateinit var materials: EditText
    private lateinit var note: EditText
    private lateinit var pay: EditText
    private lateinit var btnsave: Button
    private var taskId: Int = -1
    private var taskDb: Task? = null
    private var taskParc: TaskParcelable? = null
    private val taskviewmodel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as DataBaseApp).repo)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idFromMain = intent.getIntExtra(IntentKeys.MAIN_UPDATE_ID, -1)
        val idFromInfo = intent.getIntExtra(IntentKeys.INFO_UPDATE_ID, -1)

        if (idFromInfo == -1 && idFromMain == -1) finish()
        else if (idFromInfo != -1) taskId = idFromInfo
        else taskId = idFromMain

        lifecycleScope.launch{
            taskDb = taskviewmodel.getTask(taskId)
            if (taskDb != null) { initEditText(taskToTaskParcelable(taskDb!!)) }
        }

        btnsave = binding.btnUpdateSave.apply {
            setOnClickListener {
                taskDb = getDataFromEditText(taskId)
                if (taskDb != null){
                    intent.putExtra(IntentKeys.INFO_UPDATE_ID, taskId)
                    taskviewmodel.updateTask(taskDb!!)
                    setResult(Activity.RESULT_OK, intent)
                    NotCompletedViewWidget.forceUpdate(this@UpdateDataActivity)
                    finish()
                }
            }
        }

    }


    private fun getTaskParcelable(tp1: TaskParcelable?, tp2: TaskParcelable?): TaskParcelable?{
        if (tp1 == null && tp2 == null || tp1 != null && tp2 != null) {
            Log.i("INUPDATE", "Two TP NULL")
            return null
        }
        else if (tp1 != null) {
            Log.i("INUPDATE", "Return tp1: $tp1")
            return tp1
        }
        Log.i("INUPDATE", "Return tp2: $tp2")
        return tp2
    }

    private fun setDate(et: EditText){
        val hint = et.hint
        et.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showDatePicker(this@UpdateDataActivity, et)
            }
        }
    }

    private fun initEditText(taskp: TaskParcelable){
        dateCreate = binding.etUpdateDateCreate.apply {
            setTextPosition(this)
            setText(taskp.date_receipt)
            setDate(this)
        }
        toDate = binding.etUpdateTodate.apply {
            setTextPosition(this)
            setText(taskp.date_scheduled ?: "")
            setDate(this)
        }
        dateComplete = binding.etUpdateDateComplete.apply {
            setTextPosition(this)
            setText(taskp.date_complete ?: "")
            setDate(this)
        }
        city = binding.etUpdateCity.apply {
            setText(taskp.city ?: "")
//            setTextPosition(this)
        }
        street = binding.etUpdateStreet.apply {
            setText(taskp.street)
        }
        house = binding.etUpdateHouse.apply {
//            setTextPosition(this)
            setText(taskp.house)
        }
        apartment = binding.etUpdateApartment.apply {
            setText(taskp.apartment ?: "")
//            setTextPosition(this)
        }
        address = binding.etUpdateAddress.apply {
            setText(taskp.remainder_address ?: "")
//            setTextPosition(this)
        }
        clientName = binding.etUpdateClientName.apply {
            setText(taskp.name ?: "")
//            setTextPosition(this)
        }
        clientPhone = binding.etUpdateClientPhone.apply {
            setText(taskp.phone ?: "")
//            setTextPosition(this)
        }
        task = binding.etUpdateTask.apply {
            setText(taskp.task)
            setTextPosition(this)
        }
        completedWorks = binding.etUpdateCompletedWork.apply {
            setText(taskp.completed ?: "")
            setTextPosition(this)
        }
        note = binding.etUpdateNote.apply {
            setText(taskp.note ?: "")
            setTextPosition(this)
        }
        pay = binding.etUpdatePay.apply {
//            setTextPosition(this)
            var v = taskp.pay
            if (v == null){
                setText("")
            }
            else setText(v.toString())
        }
    }

    fun getDataFromEditText(_id: Int): Task?{
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
        if (completedWorks.text.isNotEmpty() && dateComplete.text.isEmpty()){
            Toast.makeText(this, "Поле \"дата выполнения\" не должно быть пустым", Toast.LENGTH_SHORT).show()
            return null
        }
        if (completedWorks.text.isEmpty() && dateComplete.text.isNotEmpty()){
            Toast.makeText(this, "Поле \"выполненые работы\" не должно быть пустым", Toast.LENGTH_SHORT).show()
            return null
        }

        val datecreate = dateCreate.text.toString()
        val todate = getStringOrNullFromEditText(toDate)
        val datecomplete = getStringOrNullFromEditText(dateComplete)
        val _city = getStringOrNullFromEditText(city, true)
        val _street = street.text.toString().capitalizeWords()
        val _house = house.text.toString()
        val _apartment = getStringOrNullFromEditText(apartment)
        val _address = getStringOrNullFromEditText(address)
        val name = getStringOrNullFromEditText(clientName, true)
        val phone = getStringOrNullFromEditText(clientPhone)
        val _task = task.text.toString()
        val completedworks = getStringOrNullFromEditText(completedWorks)
        val _note = getStringOrNullFromEditText(note)
        val _pay = getDoubleOrNull(pay.text)

        val iscompl = if (completedworks != null && datecomplete != null){ true } else false

        return Task(
            id = _id, date_receipt = datecreate, date_scheduled = todate, date_complete = datecomplete, city=_city, street=_street,
            house = _house, apartment = _apartment, remainder_address = _address, name = name, phone = phone,
            task = _task, note = _note, completed = completedworks, materials = null, pay = _pay, isComplete = iscompl
        )
    }






}










