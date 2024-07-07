package com.theone.busandbt.jackson

import com.theone.busandbt.utils.LOCAL_DATE_TIME_FORMATTER
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.LocalDateTime

/**
 * [String]을 [LocalDateTime]으로 변환한다.
 */
object LocalDateTimeDeserializer : StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {
    override fun deserialize(
        p: com.fasterxml.jackson.core.JsonParser?,
        ctxt: DeserializationContext?,
    ): LocalDateTime =
        LocalDateTime.parse(p?.valueAsString, LOCAL_DATE_TIME_FORMATTER)
}