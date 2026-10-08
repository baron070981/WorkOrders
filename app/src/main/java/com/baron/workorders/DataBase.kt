package com.baron.workorders

import android.R
import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase



@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var date_receipt: String, // дата поступления или создания
    var date_complete: String?, // дата выполнения
    var date_scheduled: String?, // дата на которую запланированы работы

//    @ColumnInfo(defaultValue = "'Сегежа'")
    @ColumnInfo(collate = ColumnInfo.NOCASE) var city: String?, // населенный пункт, по умолчанию Сегежа
    @ColumnInfo(collate = ColumnInfo.NOCASE) var street: String, // улица
    var house: String, // номер дома
    var apartment: String?, // номер квартиры
    @ColumnInfo(collate = ColumnInfo.NOCASE) var remainder_address: String?, // дополнительные данные адреса, например подъезд
    @ColumnInfo(collate = ColumnInfo.NOCASE) var name: String?, // имя клиента
    var phone: String?, // номер телефона для связи
    var task: String, // сама заявка
    var completed: String?, // выполненые работы или что требуется для выполнения
    var materials: String?, // использованные материалы
    var note: String?, // примечания
    var pay: Double?, // сумма к оплате, если заявка платная
    @ColumnInfo(defaultValue = "0")
    var isComplete: Boolean
)

@Database(entities = [Task::class], version = 1)   // указываем, какие сущности есть
abstract class TasksDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao   // абстрактный метод, Room его реализует

    companion object {
        @Volatile
        private var INSTANCE: TasksDatabase? = null
        fun getInstance(context: Context): TasksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TasksDatabase::class.java,
                    "tasks.db"          // имя файла базы данных
                ).createFromAsset("tasks.db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}










