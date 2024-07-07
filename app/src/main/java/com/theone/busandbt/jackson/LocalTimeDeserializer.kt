package com.theone.busandbt.jackson

import com.theone.busandbt.utils.LOCAL_DATE_TIME_FORMATTER
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.LocalTime

/**
 * [String]을 [LocalTime]으로 변환한다.
 */
object LocalTimeDeserializer : StdDeserializer<LocalTime>(LocalTime::class.java) {
    override fun deserialize(
        p: com.fasterxml.jackson.core.JsonParser?,
        ctxt: DeserializationContext?,
    ): LocalTime =
        LocalTime.parse(p?.valueAsString, LOCAL_DATE_TIME_FORMATTER)
}