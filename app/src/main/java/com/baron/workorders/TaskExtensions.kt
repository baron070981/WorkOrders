package com.baron.workorders

fun taskToTaskParcelable(task: Task): TaskParcelable{
    return TaskParcelable(
        task.id, task.date_receipt, task.date_complete, task.date_scheduled, task.city,
        task.street, task.house, task.apartment, task.remainder_address, task.name, task.phone,
        task.task, task.completed, task.materials, task.note, task.pay, task.isComplete
    )
}


fun taskParcelableToTask(task: TaskParcelable): Task{
    return Task(
        task.id, task.date_receipt, task.date_complete, task.date_scheduled, task.city,
        task.street, task.house, task.apartment, task.remainder_address, task.name, task.phone,
        task.task, task.completed, task.materials, task.note, task.pay, task.isComplete
    )
}









