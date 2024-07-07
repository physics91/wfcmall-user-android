package com.theone.busandbt.item

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.busandbt.code.Gender
import com.busandbt.code.JoinType
import com.busandbt.code.UserStatus

@Entity(tableName = "login_info")
data class LoginInfo(
    @PrimaryKey
    val id: Int,
    val token: String,
    var name: String,
    var nickname: String,
    var tel: String,
    var email: String,
    val birth: String,
    var profileImageUrl: String,
    val gender: Int = Gender.MAN.id,
    val status: Int = UserStatus.MEMBER.id,
    val joinType: Int = JoinType.NORMAL.id,
    var doAdultCert: Boolean,
    var doMarketingAgree: Boolean,
    var doNoticeSend: Boolean,
    var likeCount: Int,
    var reviewCount: Int,
    var couponCount: Int
) {
    fun getFormedToken() = "Bearer $token"
}