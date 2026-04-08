package com.myapp.creatorcollab.data.local

import androidx.room.TypeConverter
import com.myapp.creatorcollab.model.CollabType
import com.myapp.creatorcollab.model.PaymentMode
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromType(type: CollabType): String = type.name

    @TypeConverter
    fun toType(value: String): CollabType = CollabType.valueOf(value)

    @TypeConverter
    fun fromPaymentMode(mode: PaymentMode?): String? = mode?.name

    @TypeConverter
    fun toPaymentMode(value: String?): PaymentMode? = value?.let { PaymentMode.valueOf(it) }
}