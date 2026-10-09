package com.baron.workorders

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.baron.workorders.databinding.ActivityTaskInfoBinding
import kotlinx.coroutines.launch
import kotlin.getValue


const val KEY_FOR_INFO_TASK = "key_for_info"

class TaskInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskInfoBinding
    private lateinit var datecreate: TextView
    private lateinit var todate: TextView
    private lateinit var datecomplete: TextView
    private lateinit var address: TextView
    private lateinit var taskinfo: TextView
    private lateinit var completework: TextView
    private lateinit var noteinfo: TextView
    private lateinit var payinfo: TextView
    private lateinit var nameClient: TextView
    private lateinit var phoneClient: TextView
    private lateinit var btnupdate: ImageButton
    private lateinit var btndelete: ImageButton
    private lateinit var btnsend: ImageButton
    private lateinit var taskp: TaskParcelable
    private lateinit var task: Task
    private lateinit var btnphone: ImageButton
    private var taskId: Int = -1
    private var idFromMain = -1
    private var idFromInfo = -1
    private var addressString: String = ""
    private val taskviewmodel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as DataBaseApp).repo)
    }


    private val resultFromActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK){
                val _id = it.data?.getIntExtra(IntentKeys.INFO_UPDATE_ID, -1)
                if (_id != null) {taskId = _id}
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnupdate = binding.btnInfoUpdate
        btndelete = binding.btnInfoDelete
        btnsend = binding.btnInfoSend
        btnphone = binding.btnInfoPhone

        idFromMain = intent.getIntExtra(IntentKeys.MAIN_INFO_ID, -1)
        if (idFromMain == idFromInfo) finish()
        if (idFromMain != -1) taskId = idFromMain


    }

    override fun onResume() {
        super.onResume()
        if (taskId != -1) {
            lifecycleScope.launch {
                val _task = taskviewmodel.getTask(taskId)
                if (_task != null){
                    task = _task
                    initTextViews()
                    settingsTextViews(task)
                    buttonUpdateOnClick()
                    buttonDeleteOnClick()
                    buttonSendOnClick()
                    buttonPhoneOnClick()
                }
                else finish()
            }
        }
    }

    fun buttonPhoneOnClick(){
        if (task == null) return
        if (task.phone == null || task.phone!!.isEmpty()){
            btnphone.visibility = View.GONE
            btnphone.isEnabled = false
        }
        else{
            btnphone.visibility = View.VISIBLE
            btnphone.isEnabled = true
            btnphone.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${task.phone}")
                val shareIntent = Intent.createChooser(intent, "Позвонить через:")
                startActivity(shareIntent)
            }
        }
    }

    fun buttonUpdateOnClick(){
        if (task == null) return
        btnupdate.setOnClickListener {
            val intent = Intent(this, UpdateDataActivity::class.java)
            intent.putExtra(IntentKeys.INFO_UPDATE_ID, task.id)
            resultFromActivity.launch(intent)
        }
    }

    fun buttonDeleteOnClick(){
        btndelete.setOnClickListener {
            taskviewmodel.deleteTask(task)
            NotCompletedViewWidget.forceUpdate(this)
            finish()
        }
    }

    private fun createMessage(task: Task): String{
        var msg = ""
        msg += "Дата создания: ${task.date_receipt}"
        if (task.date_scheduled != null) msg += "\nНа: ${task.date_scheduled}"
        if (task.date_scheduled != null) msg += "\nВыполнена: ${task.date_complete}"
        msg += "\nАдрес: ${task.street}, ${task.house}"
        if (task.apartment != null) msg += " - ${task.apartment}"
        if (task.remainder_address != null) msg += "\n${task.remainder_address}"
        msg += "\nЗаявка:\n${task.task}"
        if (task.completed != null) msg += "\nРаботы:\n${task.completed}"
        return msg
    }

    private fun buttonSendOnClick(){
        if (task == null) return
        btnsend.setOnClickListener {
            var message = createMessage(task)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, message)
            val shareIntent = Intent.createChooser(intent, "Поделиться через:")
            startActivity(shareIntent)
        }
    }

    fun initTextViews(){
        datecreate = binding.tvInfoDateCreate
        todate = binding.tvInfoToDate
        datecomplete = binding.tvInfoDateComplete
        address = binding.tvInfoAddress
        nameClient = binding.tvInfoName
        phoneClient = binding.tvInfoPhone
        taskinfo = binding.tvInfoTask
        completework = binding.tvInfoComplete
        noteinfo = binding.tvInfoNote
        payinfo = binding.tvInfoPay
    }

    private fun settingsTextViews(task: Task) {
        addressString = createAddress(task.street, task.house, task.city, task.apartment, task.remainder_address)
        datecreate.apply {setText(task.date_receipt) }
        todate.apply { setText(task.date_scheduled ?: "нет даты") }
        datecomplete.apply { setText(task.date_complete ?: "нет даты") }
        address.apply { setText(addressString) }
        nameClient.apply { setText(task.name ?: "не известно") }
        phoneClient.apply {
            setText(task.phone ?: "не известен")
        }
        taskinfo.apply {
            setText(task.task)
        }
        completework.apply {
            if (task.completed == null || task.completed!!.isEmpty()){
                gravity = Gravity.CENTER
                setText("заявка не сделана")
            }
            else{
                gravity = Gravity.TOP or Gravity.START
                setText(task.completed)
            }
        }
        noteinfo.apply {
            setText(task.note ?: "")
        }
        payinfo.apply {
            if (task.pay == null && !task.isComplete) setText("0.0 руб.")
            else if (task.pay == null && task.isComplete) setText("бесплатная")
            else setText("${task.pay.toString()} руб.")
        }
    }




}














