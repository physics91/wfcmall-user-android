package com.theone.busandbt.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.theone.busandbt.BuildConfig
import com.theone.busandbt.R
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.api.loginchannel.VersionAPI
import com.theone.busandbt.dto.login.request.LoginByTokenRequest
import com.theone.busandbt.extension.callOnSuccess
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.model.LoginInfoViewModel
import com.theone.busandbt.type.UpdateType
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * 대기화면 코드
 * 대기화면에서 해야할 작업들을 작성한다.
 * 현재 로딩화면 구현 완료.
 * 이외 기능 추후 예정
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val loginInfoViewModel: LoginInfoViewModel by viewModel()
    private val loginAPI: LoginAPI by inject()
    private val versionAPI: VersionAPI by inject()
    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*      val currentCalendar = Calendar.getInstance()
              val targetCalendar = Calendar.getInstance().apply {
                  set(2024, Calendar.MAY, 16, 0, 0, 0)
              }

              if (currentCalendar.timeInMillis >= targetCalendar.timeInMillis) {
                  dbtEndDialog()
              } else {*/
        safeApiRequest(
            versionAPI.getLatestVersionInfo()
        ) {
            if (it.code == null) return@safeApiRequest
            if (BuildConfig.VERSION_CODE < it.code) {
                when (it.updateType) {
                    UpdateType.MANDATORY.id -> {
                        showMandatoryDialog(it.desc)
                        return@safeApiRequest
                    }

                    UpdateType.RECOMMENDED.id -> {
                        showRecommendedDialog(it.desc)
                        return@safeApiRequest
                    }

                    UpdateType.NO.id -> {} // 아무것도 하지 않는다.
                }
            }
        }
        tryLoginAndPlayApp()
        //  }
    }

    private fun dbtEndDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_dbt_end)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val closeButton = dialog.findViewById<Button>(R.id.confirmBtn)
        closeButton.setOnClickListener { AppUtils.exitApp() }
        dialog.show()
    }


    private fun showMandatoryDialog(desc: String?) {
        showMessageDialog(
            "새로운 버전이 출시되었어요!",
            desc ?: "중요한 업데이트가 있습니다! 기능 개선 및 최신 기능을 이용하려면 업데이트 버튼을 눌러주세요!",
            showCancelButton = true,
            showWarningImageView = true,
            cancelButtonText = "앱 닫기",
            cancelable = false
        ) {
            onDoneButtonClick("업데이트") {
                dismiss()
                goAppStore()
            }

            onCancelButtonClick {
                AppUtils.exitApp()
            }
        }
    }

    private fun showRecommendedDialog(desc: String?) {
        showMessageDialog(
            "새로운 버전이 출시되었어요!",
            desc ?: "앱 기능이 개선되었어요! 개선된 앱을 이용하려면 업데이트 버튼을 눌러주세요!",
            showCancelButton = true,
            showWarningImageView = true,
            cancelButtonText = "취소",
            cancelable = false
        ) {
            onCancelButtonClick {
                dismiss()
                tryLoginAndPlayApp()
            }

            onDoneButtonClick("업데이트") {
                dismiss()
                goAppStore()
            }
        }
    }

    private fun goAppStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        try {
            ActivityUtils.startActivity(intent)
        } catch (t: ActivityNotFoundException) {
            intent.data =
                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            ActivityUtils.startActivity(intent)
        }
    }

    private fun tryLoginAndPlayApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "동백통 일반"
            val channelDescription = "동백통의 일반적인 알림을 처리합니다."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        loginInfoViewModel.observe(this) {
            if (isStarted) return@observe
            isStarted = true
            if (it == null) {
                goMainActivity()
                return@observe
            }
            loginAPI.loginByToken(it.getFormedToken(), LoginByTokenRequest(it.id))
                .callOnSuccess(onFail = { _, _ ->
                    loginInfoViewModel.logout()
                    goMainActivity()
                }) { response ->
                    loginInfoViewModel.loginSuccess(response.userId, response.token) {
                        goMainActivity()
                    }
                }
        }
    }

    private fun goMainActivity(delay: Long = 1000L) {
        Handler().postDelayed({
            ActivityUtils.startActivity(MainActivity::class.java)
            finish()
        }, delay)
    }
}