package com.theone.busandbt.dto.address

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * 배달주소
 * @param id 배달주소 고유번호
 * @param name 배달주소명
 * @param jibun 지번주소
 * @param road 도로명주소
 * @param addressDetail 상세주소
 * @param lat 위도
 * @param lng 경도
 * @param enabled 배달주소 활성화(선택) 유무, true면 활성화
 */
@Parcelize
@Entity(tableName = "address_table")
data class DeliveryAddress(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var deliveryAddressId: Int,
    var memberId: Int,
    var name: String,
    var jibun: String,
    var road: String,
    var addressDetail: String,
    var lat: Double,
    var lng: Double,
    var enabled: Boolean
): Parcelable {
    companion object {
        fun createEmpty(name: String) = DeliveryAddress(0, 0, 0, name, "", "", "", 0.0, 0.0, false)
    }
}
