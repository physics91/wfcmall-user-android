package com.theone.busandbt.dto.member

import android.os.Parcelable
import com.busandbt.code.Gender
import com.busandbt.code.JoinType
import com.busandbt.code.MemberGrade
import com.busandbt.code.UserStatus
import kotlinx.parcelize.Parcelize

/**
 * 회원정보 데이터
 * @param grade 회원등급 [MemberGrade]
 * @param gender 성별 [Gender]
 * @param status 회원상태 [UserStatus]
 * @param joinType 가입유형 [JoinType]
 * @param doAdultCert 성인인증 여부
 */
@Parcelize
data class MemberDetail(
    val grade: Int,
    val name: String,
    val tel: String,
    val email: String,
    val birth: String,
    val gender: Int,
    val status: Int,
    val joinType: Int,
    val profileImageUrl: String,
    val doAdultCert: Boolean,
    val doEssentialAgree: Boolean,
    val doDeliveryStatusSend: Boolean,
    val doPushSend: Boolean,
    val doSMSSend: Boolean,
    val doEmailSend: Boolean,
    val likeCount: Int,
    val reviewCount: Int,
    val couponCount: Int,
    val nickname: String
) : Parcelable