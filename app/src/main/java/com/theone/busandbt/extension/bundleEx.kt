package com.theone.busandbt.extension

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.fasterxml.jackson.module.kotlin.readValue
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import java.io.Serializable

fun Bundle?.getServiceType(default: ServiceType = ServiceType.FOOD_DELIVERY): ServiceType {
    if (this == null) return default
    return ServiceType.find(getInt("serviceTypeId", default.id))
}

fun Bundle?.getDeliveryType(default: DeliveryType = DeliveryType.INSTANT): DeliveryType {
    if (this == null) return default
    return DeliveryType.find(getInt("deliveryTypeId", default.id))
}

inline fun <reified T: Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T: Parcelable> Bundle.getParcelableArrayCompat(key: String): Array<T> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArray(key, T::class.java) as Array<T>
    } else {
        getParcelableArray(key) as Array<T>
    }
}

inline fun <reified T: Serializable> Bundle.getSerializableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}