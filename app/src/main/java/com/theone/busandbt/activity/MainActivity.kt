package com.theone.busandbt.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.databinding.ActivityMainBinding
import com.theone.busandbt.dto.coupon.request.IssueCouponRequest
import com.theone.busandbt.eventbus.ToggleProgressEvent
import com.theone.busandbt.extension.callOnSuccess
import com.theone.busandbt.extension.getSerializableCompat
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.listener.BackButtonInterceptor
import com.theone.busandbt.model.AppViewModel
import com.theone.busandbt.model.BasketListViewModel
import com.theone.busandbt.model.DeliveryAddressViewModel
import com.theone.busandbt.model.JoinInfoViewModel
import com.theone.busandbt.model.LoginInfoViewModel
import com.theone.busandbt.utils.FCMUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Duration
import java.time.LocalDateTime

/**
 * 주 화면 코드
 * 세부 화면들은 모두 Fragment로 처리한다.
 * 추후에 작성
 */
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val appViewModel: AppViewModel by viewModel()
    private val basketListViewModel: BasketListViewModel by viewModel()
    private val loginInfoViewModel: LoginInfoViewModel by viewModel()
    private val deliveryAddressViewModel: DeliveryAddressViewModel by viewModels()
    private val joinInfoViewModel: JoinInfoViewModel by viewModel()
    private val couponAPI: CouponAPI by inject()

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val seconds = joinInfoViewModel.getCurrentJoinSmsExpireSeconds()
        outState.putInt("joinSmsExpireSeconds", seconds)
        outState.putSerializable("joinSmsSavedDateTime", LocalDateTime.now())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val seconds = savedInstanceState.getInt("joinSmsExpireSeconds", 0)
        val now = LocalDateTime.now()
        val savedDateTime =
            savedInstanceState.getSerializableCompat<LocalDateTime>("joinSmsSavedDateTime")
        val remainSeconds =
            (seconds - Duration.between(now, savedDateTime).seconds).takeIf { it > 0 } ?: 0
        joinInfoViewModel.setJoinSmsExpireSeconds(remainSeconds.toInt())
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        if (savedInstanceState != null) {
            val seconds = savedInstanceState.getInt("joinSmsExpireSeconds", 0)
            val now = LocalDateTime.now()
            val savedDateTime =
                savedInstanceState.getSerializableCompat<LocalDateTime>("joinSmsSavedDateTime")
            val remainSeconds =
                (seconds - Duration.between(now, savedDateTime).seconds).takeIf { it > 0 } ?: 0
            joinInfoViewModel.setJoinSmsExpireSeconds(remainSeconds.toInt())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        loginInfoViewModel.initLiveData.observe(this) {
            processDeepLinkIntent(intent)
        }

        loginInfoViewModel.loginInfoLiveData.observe(this) {
            deliveryAddressViewModel.init(it?.getFormedToken(), it?.id)
            if (it != null) FCMUtils.login(it.id)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
            val navController = navHostFragment.navController
            appBarConfiguration = AppBarConfiguration(navController.graph)
            mainToolbarContent.goBackButton.setOnClickListener {
                val currentFragment = navHostFragment.childFragmentManager.fragments[0]
                if (currentFragment is BackButtonInterceptor && !currentFragment.interceptOnBackButtonClicked()) return@setOnClickListener
                navController.popBackStack()
            }
            mainToolbarContent.goBasketButtonSpace.setOnClickListener {
                val dest = navController.currentDestination ?: return@setOnClickListener
                navController.navigate(
                    R.id.basket_graph,
                    BasketMainFragmentArgs(
                        appViewModel.basketServiceType.id,
                        appViewModel.basketDeliveryType.id
                    ).toBundle(),
                    NavOptions.Builder().setPopUpTo(dest.id, false).setRestoreState(true).build()
                )
            }

            Glide.with(this@MainActivity)
                .load(R.drawable.ic_progress)
                .into(progressImageView)

            appViewModel.observeProgressBar(this@MainActivity) {
                progressImageView.isVisible = it
            }
            appViewModel.observeActionBarTitle(this@MainActivity) {
                mainToolbar.isVisible = it.isNotEmpty()
                mainToolbarContent.basketGroup.isVisible = it.isNotEmpty()
                mainToolbarContent.basketCountTextView.isVisible =
                    mainToolbarContent.basketCountTextView.text != "0"
                mainToolbarContent.titleTextView.text = it
            }

            appViewModel.observeGoBasketVisible(this@MainActivity) {
                mainToolbarContent.basketGroup.isVisible = it
                val countText = mainToolbarContent.basketCountTextView.text
                if (it) mainToolbarContent.basketCountTextView.isVisible = countText != "0"
            }

            appViewModel.observeGoBackButtonVisible(this@MainActivity) {
                // 현재 해당 백버튼이 안보일 경우 디자인이 깨져 모두 보이게 하는 방향으로 유지
//                mainToolbarContent.goBackButton.isVisible = it
            }

            basketListViewModel.observe(this@MainActivity) {
                var totalCount = 0
                it.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                mainToolbarContent.basketCountTextView.text = "$totalCount"
                if (mainToolbarContent.basketGroup.isVisible) mainToolbarContent.basketCountTextView.isVisible =
                    totalCount != 0
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processDeepLinkIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        FCMUtils.unregister(loginInfoViewModel.getLoginInfo()?.id)
    }

    /**
     * 딥링크 데이터를 체크하고 그에 맞는 처리를 한다.
     */
    private fun processDeepLinkIntent(intent: Intent?) {
        val data = intent?.data ?: return
        val loginInfo = loginInfoViewModel.getLoginInfo()
        val action = data.getQueryParameter("action")?.toIntOrNull() ?: return
        when (action) {
            1 -> {
                if (loginInfo == null) {
                    suggestLogin()
                    return
                }
                val couponId = data.getQueryParameter("couponId")?.toIntOrNull() ?: return
                couponAPI.issueCoupon(
                    loginInfo.getFormedToken(),
                    IssueCouponRequest(memberId = loginInfo.id, couponIdList = listOf(couponId))
                ).callOnSuccess(showFailMessage = true) {
                    showMessageDialog("쿠폰 등록이 완료되었습니다.")
                }
            }
        }
    }

    private fun suggestLogin() {
        val controller = findNavController(R.id.nav_host_fragment_content_main)
        val dest = controller.currentDestination ?: return
        showMessageDialog(
            "로그인이 필요한 서비스 입니다.",
            "로그인 화면으로 이동합니다.",
            showWarningImageView = true,
            showCancelButton = true
        ) {
            onDoneButtonClick {
                dismiss()
                controller.navigate(
                    R.id.login_graph,
                    null,
                    NavOptions.Builder().setPopUpTo(dest.id, false).build()
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleProgress(event: ToggleProgressEvent) {
        appViewModel.showProgress(event.showingProgress)
    }
}