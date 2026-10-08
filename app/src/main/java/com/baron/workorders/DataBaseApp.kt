package com.baron.workorders

import android.app.Application
import androidx.activity.viewModels
import kotlin.getValue

class DataBaseApp: Application() {

    val db by lazy { TasksDatabase.getInstance(applicationContext)}
    private val taskdao by lazy {db.taskDao()}
    val repo by lazy { NoteRepo(taskdao) }
}