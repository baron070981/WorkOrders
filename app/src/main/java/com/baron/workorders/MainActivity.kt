package com.baron.workorders

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baron.workorders.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMenuListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var listtasks: RecyclerView
    private lateinit var spinnerviewcontent: Spinner
    private lateinit var btnadd: ImageButton
    private lateinit var btnsearch: ImageButton
    private lateinit var search: EditText
    private lateinit var btnsend: ImageButton
    private lateinit var btnphone: ImageButton
    private lateinit var btndelete: ImageButton
    private lateinit var btnedit: ImageButton
    private lateinit var tvCountTasks: TextView
    lateinit var taskadapter: TaskAdapter
    private var selectedItemPosition = -1 //
    private var selectedTask: Task? = null //
//    private var counterTasks: String = "" //
    private val menuviewcontent = listOf("все заявки", "в работе", "сделано")
    private var countNotComplete = 0
    private var countComplete = 0

    private val taskviewmodel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as DataBaseApp).repo)
    }

    private val resultFromActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK){

            }
        }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            showButtonAdd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootLayout = binding.root
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        tvCountTasks = binding.tvNotcompleteCount
        listtasks = binding.listnotes as RecyclerView
        btnsearch = binding.btnSearch
        search = binding.etSearch
        btnedit = binding.btnBottomEdit
        btndelete = binding.btnBottomDelete
        btnsend = binding.btnBottomSend
        btnphone = binding.btnBottomPhone
        btnadd = binding.btnadd.apply {clearColorFilter()}
        spinnerviewcontent = binding.spinnerViewContent

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        taskadapter = TaskAdapter(this ){ clickedNote ->
            onItemClick(clickedNote)
            showButtonAdd()
        }
        listtasks.layoutManager = LinearLayoutManager(this)
        listtasks.adapter = taskadapter
        listtasks.setOnClickListener { showButtonAdd() }
        rootLayout.setOnClickListener { showButtonAdd() }

        val spinnerviewcontentadapter = ArrayAdapter(
            this,
            R.layout.spinner_normal,
            menuviewcontent
        )
        spinnerviewcontentadapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerviewcontent.adapter = spinnerviewcontentadapter
        spinnerviewcontent.onItemSelectedListener = SpinnerListener(){ position ->
            when (position){
                0 -> getAllTasks()
                1 -> getNotCompleted()
                2 -> getCompleted()
            }
        }

        searchResultObserve() //
        buttonSearchOnClick() //
        buttonAddNewTaskOnClick() //
        btnedit.setOnClickListener { clickButtonEdit() }
        btnphone.setOnClickListener { clickButtonPhone() }
        btnsend.setOnClickListener { clickButtonSend() }
        btndelete.setOnClickListener { clickButtonDelete() }

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val countNotComplete = taskviewmodel.getCountNotCompleteInt()
            val countComplete = taskviewmodel.getCountAllInt()
            val counterTasks = "всего: $countComplete / в работе: $countNotComplete"
            tvCountTasks.setText(counterTasks)
        }
    }

    private fun showButtonAdd(){
        /** кноака создания новой заявки становится вимой и кликабельной
        *  а кнопки меню наоборот становятся невидимыми и не кликабельными
        */
        btnadd.apply {visibility = View.VISIBLE; isEnabled = true}
        btnedit.apply { visibility = View.GONE; isEnabled = false }
        btndelete.apply { visibility = View.GONE; isEnabled = false }
        btnsend.apply { visibility = View.GONE; isEnabled = false }
        btnphone.apply { visibility = View.GONE; isEnabled = false }
        selectedTask = null
        selectedItemPosition = -1
        onBackPressedCallback.isEnabled = false
    }

    private fun showButtonsMenu(phone: String?){
        /* кнопки меню становятся видимыми и кликабельным,
        *  а кнопка добавления становится невидимой и не кликабельной
        * */
        btnadd.apply { visibility = View.GONE; isEnabled = false }
        btnedit.apply { visibility = View.VISIBLE; isEnabled = true }
        btndelete.apply { visibility = View.VISIBLE; isEnabled = true }
        btnsend.apply { visibility = View.VISIBLE; isEnabled = true }
        if (phone != null) btnphone.apply { visibility = View.VISIBLE; isEnabled = true }
        else btnphone.apply { visibility = View.GONE; isEnabled = false }
        onBackPressedCallback.isEnabled = true
    }

    override fun onLongClick(position: Int, task: Task) {
        /**
            обработка долгого нажатия. запоминается позиция и запись в бд
            показывается меню с кнопками: отправить, позвонить(если есть номер),
            редактировать и удалить
        */
        selectedItemPosition = position
        selectedTask = task
        showButtonsMenu(task.phone)
    }

    private fun clickButtonEdit(){
        /** переход в активити редактирования записи */
        if (selectedTask == null) return
        val intent = Intent(this, UpdateDataActivity::class.java)
        intent.putExtra(IntentKeys.MAIN_UPDATE_ID, selectedTask!!.id)
        resultFromActivity.launch(intent)
        showButtonAdd()
    }

    private fun clickButtonPhone(){
        /* перейти в звонилку */
        if (selectedTask == null) return
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${selectedTask!!.phone}")
        val shareIntent = Intent.createChooser(intent, "Позвонить через:")
        showButtonAdd()
        startActivity(shareIntent)
    }

    private fun clickButtonSend(){
        /* перейти в приложение для отправки сообщений */
        var msg = "Дата создания: ${selectedTask!!.date_receipt}"
        if (selectedTask!!.date_complete != null) msg += "\nВыполнена: ${selectedTask!!.date_complete}"
        msg += "\nАдрес: ${selectedTask!!.street} ${selectedTask!!.house}"
        if (selectedTask!!.apartment != null) msg += " - ${selectedTask!!.apartment}"
        msg += "\nЗаявка: ${selectedTask!!.task}"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        val shareIntent = Intent.createChooser(intent, "Поделиться через:")
        showButtonAdd()
        startActivity(shareIntent)
    }

    private fun clickButtonDelete(){
        /* удаление записи */
        taskviewmodel.deleteTask(selectedTask!!)
        NotCompletedViewWidget.forceUpdate(this)
        showButtonAdd()
    }

    private fun buttonSearchOnClick(){
        btnsearch.setOnClickListener {
            val query = search.text.toString().trim()
            taskviewmodel.searchTasks(query)
        }
    }

    private fun searchResultObserve(){
        taskviewmodel.searchResults.observe(this){ tasks ->
            taskadapter.submitList(tasks)
        }
    }

    private fun getAllTasks() {
        taskviewmodel.allTasks.observe(this@MainActivity){ task ->
            taskadapter.submitList(task)
        }
    }

    private fun getNotCompleted(){
        taskviewmodel.notCompletedTasks.observe(this@MainActivity){ task ->
            taskadapter.submitList(task)
        }
    }

    private fun getCompleted() {
        taskviewmodel.completedTasks.observe(this@MainActivity){ task ->
            taskadapter.submitList(task)
        }
    }

    private fun buttonAddNewTaskOnClick(){
        btnadd.setOnClickListener {
            val intent = Intent(this, InsertDataActivity::class.java)
            resultFromActivity.launch(intent)
        }
    }

    private fun onItemClick(task: Task){
        val intent = Intent(this, TaskInfoActivity::class.java)
        intent.putExtra(IntentKeys.MAIN_INFO_ID, task.id)
        resultFromActivity.launch(intent)
    }

    // сохранение данных при уничтожении активити
    // например при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    // востановление данных при пересоздании активити
    // например при повороте экрана
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }


}







