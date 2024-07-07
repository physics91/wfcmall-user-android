package com.theone.busandbt.type

enum class PromotionScreenType(val id: Int) {
    MAIN_SCREEN(1),
    DELIVERY_TAB(2),
    TAKEAWAY_TAB(3),
    PARCEL_DELIVERY_TAB(4),
    PARCEL_PRODUCT_LIST(5);

    companion object {
        fun fromTabPosition(position: Int): PromotionScreenType? {
            return when(position) {
                0 -> DELIVERY_TAB
                1 -> TAKEAWAY_TAB
                2 -> PARCEL_DELIVERY_TAB
                else -> null
            }
        }
    }
}