package com.baron.workorders

import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize


@Parcelize
data class TaskParcelable(
    var id: Int = -1,
    var date_receipt: String, // дата поступления или создания
    var date_complete: String?, // дата выполнения
    var date_scheduled: String?, // дата на которую запланированы работы

    var city: String?, // населенный пункт, по умолчанию Сегежа
    var street: String, // улица
    var house: String, // номер дома
    var apartment: String?, // номер квартиры
    var remainder_address: String?, // дополнительные данные адреса, например подъезд
    var name: String?, // имя клиента
    var phone: String?, // номер телефона для связи
    var task: String, // сама заявка
    var completed: String?, // выполненые работы или что требуется для выполнения
    var materials: String?, // использованные материалы
    var note: String?, // примечания
    var pay: Double?, // сумма к оплате, если заявка платная
    var isComplete: Boolean
): Parcelable












