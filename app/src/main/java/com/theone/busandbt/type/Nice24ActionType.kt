package com.theone.busandbt.type

import com.busandbt.code.IdBaseType
import com.fasterxml.jackson.annotation.JsonValue

enum class Nice24ActionType(@JsonValue override val id: Int) : IdBaseType {
    FIND_PASSWORD(1),
    NORMAL_JOIN(2),
    SNS_JOIN(3),
    ;

    companion object {
        val VALUES = values().associateBy { it.id }
        fun find(id: Int) = VALUES[id]
    }
}