package com.baron.workorders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.launch


@Dao
interface TaskDao {
    @Insert   // Room сам напишет INSERT
    suspend fun insert(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY id DESC LIMIT 100")   // Room выполнит этот SQL
    fun getAll(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isComplete = 1  ORDER BY id DESC LIMIT 100")
    fun getCompleted(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isComplete = 0  ORDER BY id DESC  LIMIT 100")
    fun getNotCompleted(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskid")
    suspend fun getTask(taskid:Int): Task?

    @Query("SELECT * FROM tasks WHERE id = :taskid LIMIT 1")
    fun getTaskById(taskid:Int): LiveData<Task?>

    @Update
    suspend fun update(task: Task)

    @Query("SELECT COUNT(*) FROM tasks WHERE isComplete = 0")
    fun getCountNotComplete(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isComplete = 0")
    suspend fun getCountNotCompleteInt(): Int

    @Query("SELECT COUNT(*) FROM tasks")
    fun getCountAll(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getCountAllInt(): Int

    @Query("""
        SELECT * FROM tasks 
        WHERE street LIKE '%' || :query || '%' 
           OR house LIKE '%' || :query || '%' 
           OR apartment LIKE '%' || :query || '%' 
           OR date_complete LIKE '%' || :query || '%' 
           OR date_receipt LIKE '%' || :query || '%' 
           OR (street || ' ' || house) LIKE '%' || :query || '%'
           OR (street || ' ' || house || ' ' || apartment) LIKE '%' || :query || '%'
           ORDER BY id DESC LIMIT 100
    """)
    fun searchTasks(query: String): LiveData<List<Task>>
}

// репозиторий. прослойка между dao и viewmodel
class NoteRepo(private val taskDao: TaskDao) {
    val allTasks: LiveData<List<Task>> = taskDao.getAll()
    val completedTasks: LiveData<List<Task>> = taskDao.getCompleted()
    val notCompletedTasks: LiveData<List<Task>> = taskDao.getNotCompleted()
    suspend fun insert(task:Task) = taskDao.insert(task)
    suspend fun deleteNote(task:Task) = taskDao.deleteTask(task)
    suspend fun update(task: Task) = taskDao.update(task)
    suspend fun getTask(taskid: Int): Task? = taskDao.getTask(taskid)
    suspend fun getCountNotCompleteInt() = taskDao.getCountNotCompleteInt()
    suspend fun getCountaAllInt() = taskDao.getCountAllInt()
    val countNotComplete: LiveData<Int> = taskDao.getCountNotComplete()
    val countAll: LiveData<Int> = taskDao.getCountAll()
    fun searchTask(query: String): LiveData<List<Task>> = taskDao.searchTasks(query)

}

// фабрика для viewmodel
class TaskViewModelFactory(private val repository: NoteRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// viewmodel
class TaskViewModel(private val repo: NoteRepo) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Task>>()
    val searchResults: LiveData<List<Task>> = _searchResults
    private var currentDbSource: LiveData<List<Task>>? = null
    private val dbObserver = Observer<List<Task>> { tasks ->
        _searchResults.value = tasks
    }

    val allTasks: LiveData<List<Task>> = repo.allTasks
    val completedTasks: LiveData<List<Task>> = repo.completedTasks
    val notCompletedTasks: LiveData<List<Task>> = repo.notCompletedTasks
    val countNotComplete: LiveData<Int> = repo.countNotComplete
    val countAll: LiveData<Int> = repo.countAll

    fun searchTasks(query: String){
        currentDbSource?.removeObserver(dbObserver)
        val newSource = repo.searchTask(query)
        currentDbSource = newSource
        newSource.observeForever(dbObserver)
    }

    override fun onCleared() {
        super.onCleared()
        currentDbSource?.removeObserver(dbObserver)
    }

    fun addTask(task: Task) {
        // viewModelScope автоматически запускает корутину на Dispatchers.Main,
        // но Room сам переключит поток на фоновый (Dispatchers.IO) под капотом,
        // так как функция объявлена как suspend.
        viewModelScope.launch {
            repo.insert(task)
        }
    }

    fun deleteTask(task: Task){
        viewModelScope.launch {
            repo.deleteNote(task)
        }
    }

    fun updateTask(task: Task){
        Log.i(LogKeys.INFOACTIVITY, "update task fun in DBH: id = ${task.id}")
        viewModelScope.launch {
            repo.update(task)
        }
    }

    suspend fun getTask(id: Int): Task? {
        return repo.getTask(id)
    }

    suspend fun getCountNotCompleteInt(): Int {
        return repo.getCountNotCompleteInt()
    }

    suspend fun getCountAllInt(): Int {
        return repo.getCountaAllInt()
    }


}















