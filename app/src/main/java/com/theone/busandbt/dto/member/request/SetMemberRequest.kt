package com.theone.busandbt.dto.member.request

/**
 * 회원 수정 요청 데이터
 * @param birth "2022-11-22" 형식으로 입력할 것
 */
data class SetMemberRequest(
    val name: String? = null,
    val nickname: String? = null,
    val tel: String? = null,
    val email: String? = null,
    val birth: String? = null,
    val doAdultCert: Boolean? = null,
    val doEventSend: Boolean? = null,
    val doDeliveryStatusSend: Boolean? = null,
    val doPushSend: Boolean? = null,
    val doSMSSend: Boolean? = null,
    val doEmailSend: Boolean? = null,
    val file: ProfileImageFile? = null,
    val doRemoveProfileImage: Boolean? = null
) {
    data class ProfileImageFile(
        val encodedFile: String,
        val fileName: String,
        val fileExtension: String
    )
}