package com.theone.busandbt.jackson

import com.theone.busandbt.utils.LOCAL_DATE_TIME_FORMATTER
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalDateTime

/**
 * [LocalDateTime]을 [String]로 변환한다.
 */
object LocalDateTimeSerializer : StdSerializer<LocalDateTime>(LocalDateTime::class.java) {
    override fun serialize(
        value: LocalDateTime?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value?.format(LOCAL_DATE_TIME_FORMATTER))
    }
}