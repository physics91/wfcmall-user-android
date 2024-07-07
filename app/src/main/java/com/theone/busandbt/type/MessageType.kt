package com.theone.busandbt.type

import com.busandbt.code.IdBaseType
import com.fasterxml.jackson.annotation.JsonValue

/**
 * 알림 메시지 타입
 */
enum class MessageType(@JsonValue override val id: Int) : IdBaseType {
    NOTICE_ADD(1),
    ORDER_ADD(2),
    ORDER_MANAGE(3),
    ORDER_SET(4),
    ORDER_STATUS_SET(5),
    ORDER_PACKAGING_DONE(6),
    SHOP_SET(7),
    LOWER_SHOP_SET(8),
    RIDER_SET(9),
    ;
}