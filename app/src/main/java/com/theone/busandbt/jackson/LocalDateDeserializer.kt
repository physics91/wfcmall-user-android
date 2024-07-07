package com.theone.busandbt.jackson

import com.theone.busandbt.utils.LOCAL_DATE_TIME_FORMATTER
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.LocalDate

/**
 * [String]을 [LocalDate]로 변환한다.
 */
object LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {
    override fun deserialize(
        p: com.fasterxml.jackson.core.JsonParser?,
        ctxt: DeserializationContext?,
    ): LocalDate =
        LocalDate.parse(p?.valueAsString, LOCAL_DATE_TIME_FORMATTER)
}