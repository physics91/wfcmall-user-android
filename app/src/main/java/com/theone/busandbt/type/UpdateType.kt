package com.theone.busandbt.type

import com.busandbt.code.IdBaseType

enum class UpdateType(override val id: Int) : IdBaseType {
    MANDATORY(1),
    RECOMMENDED(2),
    NO(3),
    ;

    companion object {
        val VALUES_MAP = values().associateBy { it.id }
        fun find(id: Int) = VALUES_MAP[id]
    }
}