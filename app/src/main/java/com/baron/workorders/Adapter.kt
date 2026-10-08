package com.baron.workorders


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView




// ==========================================================

const val MAX_LEN_TASK_INFO = 25

class TaskAdapter(private val menuClickListener: OnMenuListener,
                  private val onItemClicked:(Task)->Unit):
    ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    // ViewHolder удерживает ссылки на View каждого элемента
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvdate: TextView = itemView.findViewById(R.id.tv_item_create_date)
        val tvdatecompl: TextView = itemView.findViewById(R.id.tv_item_complete_date)
        val tvtodate: TextView = itemView.findViewById(R.id.tv_item_to_date)
        val tvlabeltodate: TextView = itemView.findViewById(R.id.tv_item_label_to_date)
        val tvaddress: TextView = itemView.findViewById(R.id.tv_item_address)
        val tvtaskcontent: TextView = itemView.findViewById(R.id.tv_item_task_content)
        val marker: View = itemView.findViewById(R.id.vmarker)
        val labelDateComplete: TextView = itemView.findViewById(R.id.tv_label_date_complete)
        val markerPhone: ImageView = itemView.findViewById(R.id.iv_item_marker_phone)

        fun createAddress(task: Task): String{
            var addressString: String = ""
            val street = task.street
            val house = task.house
            addressString += "ул.$street д.$house"
            if (task.apartment != null && task.apartment.toString().isNotEmpty()){
                addressString += " кв.${task.apartment}"
            }
            if (task.remainder_address != null && task.remainder_address.toString().isNotEmpty()){
                addressString += " ${task.remainder_address}"
            }

            return addressString
        }

        fun bind(task: Task, position:Int){
//            tvdate.text = note.date
            tvdate.text = task.date_receipt
            tvdatecompl.text = task.date_complete ?: ""
            tvtodate.text = task.date_scheduled ?: ""
            tvaddress.text = createAddress(task)
            tvtaskcontent.text = if (task.task.length > MAX_LEN_TASK_INFO){task.task.take(25) + "..."}
                                 else task.task

            if (task.date_scheduled == null){
                tvlabeltodate.visibility = View.GONE
                tvtodate.visibility = View.GONE
            }
            else{
                tvlabeltodate.visibility = View.VISIBLE
                tvtodate.visibility = View.VISIBLE
            }
            if (!task.isComplete || task.completed == null){
                marker.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.marker_not_complete))
                labelDateComplete.setText("")
            }
            else {
                marker.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.marker_completed))
                labelDateComplete.setText("выполнена")
            }
            if (task.phone == null){
                markerPhone.visibility = View.GONE
                markerPhone.isEnabled = false
            }
            else{
                markerPhone.visibility = View.VISIBLE
                markerPhone.isEnabled = true
            }
        }
    }

    // Создает новый ViewHolder при нехватке на экране
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return TaskViewHolder(view)
    }

    // Заполняет ViewHolder данными для конкретной позиции
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(task)
        }
        holder.itemView.setOnLongClickListener { view ->
            menuClickListener.onLongClick(position, task)
            true
        }
        holder.bind(task, position+1)
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id // Сравниваем по уникальному ID
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem // Сравниваем все поля объекта
    }
}

// интерфейс для открытия кнопок меню при долгом нажатии
interface OnMenuListener {
    fun onLongClick(position: Int, task: Task)
}
























