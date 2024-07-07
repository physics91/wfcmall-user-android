package com.theone.busandbt.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.SPUtils
import com.busandbt.code.OrderStatus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.theone.busandbt.R
import com.theone.busandbt.activity.MainActivity
import com.theone.busandbt.eventbus.ChangeOrderStatusEvent
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.type.MessageType
import com.theone.busandbt.utils.APP_SETTINGS_KEY
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import org.greenrobot.eventbus.EventBus

/**
 * FCM 수신 서비스 구현
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.apply()
        debugLog(TAG, "서비스 토큰 : $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val message = remoteMessage.notification?.body

        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message title: $title")
        Log.d(TAG, "Message body: $message")
        Log.d(TAG, "From: ${remoteMessage.data}")
        val data = remoteMessage.data
        showNotification(data)
    }

    /**
     * TODO - 메시지 유형별로 알림 채널 분리가 필요함
     */
    private fun showNotification(data: Map<String, String>) {
        val messageType = data["messageType"]?.toIntOrNull() ?: return
        val innerData = data["data"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return
        when (messageType) {
            MessageType.ORDER_STATUS_SET.id -> {
                val json = JACKSON_OBJECT_MAPPER.readTree(innerData)
                EventBus.getDefault().post(
                    ChangeOrderStatusEvent(
                        json.get("orderId").asText(),
                        OrderStatus.find(json.get("oldStatus").asInt()),
                        OrderStatus.find(json.get("status").asInt()),
                        if (json.has("orderDoneMinutes")) json.get("orderDoneMinutes")
                            .asInt() else null
                    )
                )
            }

            MessageType.ORDER_PACKAGING_DONE.id -> {
                val json = JACKSON_OBJECT_MAPPER.readTree(innerData)
                EventBus.getDefault().post(
                    ChangeOrderStatusEvent(
                        json.get("orderId").asText(),
                        OrderStatus.START_COOKING,
                        OrderStatus.COMPLETE_PACKAGING,
                        null
                    )
                )
            }

            else -> {}
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_app)

        val sp = SPUtils.getInstance(APP_SETTINGS_KEY)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val useVibrateAlert = sp.getBoolean("useVibrateAlert", true)
        val useSoundAlert = sp.getBoolean("useSoundAlert", true)
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        if (useVibrateAlert && audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT) playVibrator()
        if (useSoundAlert && audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL && volume > 0) playRingtone()
        notificationManager.notify(0, notificationBuilder.build()) // 알림 생성
    }

    private fun playRingtone() {
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notificationSoundUri)
        ringtone.play()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.volume = 1.0f
    }

    private fun playVibrator() {
        val duration = 1000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            )
        } else {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(
                    duration, // 진동 지속 시간 (예: 200ms)
                    VibrationEffect.DEFAULT_AMPLITUDE // 기본 진동 강도
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                // 이전 버전에서는 직접 진동 시간을 지정
                vibrator.vibrate(duration)
            }
        }
    }
}