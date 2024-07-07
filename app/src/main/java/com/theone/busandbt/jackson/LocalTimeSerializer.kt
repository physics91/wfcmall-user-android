package com.theone.busandbt.jackson

import com.theone.busandbt.utils.LOCAL_TIME_FORMATTER
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalTime

/**
 * [LocalTime]을 [String]로 변환한다.
 */
object LocalTimeSerializer : StdSerializer<LocalTime>(LocalTime::class.java) {
    override fun serialize(
        value: LocalTime?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value?.format(LOCAL_TIME_FORMATTER))
    }
}