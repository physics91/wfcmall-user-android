package com.theone.busandbt.jackson

import com.theone.busandbt.utils.LOCAL_DATE_FORMATTER
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalDate

/**
 * [LocalDate]를 [String]으로 변환한다.
 */
object LocalDateSerializer : StdSerializer<LocalDate>(LocalDate::class.java) {

    override fun serialize(
        value: LocalDate?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.writeString(value?.format(LOCAL_DATE_FORMATTER))
    }
}