package com.theone.busandbt.utils

import com.theone.busandbt.extension.debugLog
import com.busandbt.code.ChannelType
import com.busandbt.code.UserType
import com.theone.busandbt.extension.infoLog
import com.google.firebase.messaging.FirebaseMessaging

object FCMUtils {

    /**
     * TODO - TOO MANY REGISTER
     */
    fun register(memberId: Int? = null) {
        with(FirebaseMessaging.getInstance()) {
            val key = "android-${ChannelType.DONG_BAEK.id}-noticeAdd-${UserType.MEMBER.id}"
            debugLog("FCM 공지", key)
            subscribeToTopic(key)
                .addOnCompleteListener {
                    debugLog("FCM 공지", it.isSuccessful)
                }
                .addOnFailureListener {
                    debugLog("FCM 공지", "실패")
                }
                .addOnCanceledListener {
                    debugLog("FCM 공지", "취소")
                }
                .addOnSuccessListener {
                    debugLog("FCM 공지", "성공")
                }

            if (memberId != null) { login(memberId) }
        }
    }

    fun unregister(memberId: Int? = null) {
        with(FirebaseMessaging.getInstance()) {
            unsubscribeFromTopic("android-${ChannelType.DONG_BAEK.id}-noticeAdd-${UserType.MEMBER.id}")
                .addOnCompleteListener {
                    infoLog("FCM 공지 해제", it.isSuccessful)
                }
            if (memberId != null) { logout(memberId) }
        }
    }

    fun logout(memberId: Int) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("android-${ChannelType.DONG_BAEK.id}-${UserType.MEMBER.id}-${memberId}")
            .addOnCompleteListener {
                infoLog("FCM 개인 해제", it.isSuccessful)
            }
    }

    fun login(memberId: Int) {
        FirebaseMessaging.getInstance().subscribeToTopic("android-${ChannelType.DONG_BAEK.id}-${UserType.MEMBER.id}-${memberId}")
            .addOnCompleteListener {
                infoLog("FCM 개인", it.isSuccessful)
            }
    }
}