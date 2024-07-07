package com.theone.busandbt.fragment.main

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.bumptech.glide.Glide
import com.busandbt.code.DeliveryType
import com.busandbt.code.PromotionStatus
import com.busandbt.code.PromotionType
import com.busandbt.code.ServiceType
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient
import com.theone.busandbt.R
import com.theone.busandbt.adapter.promotion.BannerViewPagerAdapter
import com.theone.busandbt.api.orderchannel.NoticeAPI
import com.theone.busandbt.api.orderchannel.PromotionAPI
import com.theone.busandbt.databinding.FragmentMainBinding
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.promotion.PromotionDetailFragmentArgs
import com.theone.busandbt.fragment.shop.ServiceMainFragmentArgs
import com.theone.busandbt.model.AppViewModel
import com.theone.busandbt.model.BannerViewModel
import com.theone.busandbt.model.main.MainViewModel
import com.theone.busandbt.type.PromotionScreenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.LocalDate


/**
 * 메인 화면
 * 시작할때 팝업창 띄워지고, 음식점, 전통시장 프래그먼트와 연결되어있다.
 */
class MainFragment : DataBindingFragment<FragmentMainBinding>(), View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_main
    private lateinit var bannerViewPagerAdapter: BannerViewPagerAdapter
    private lateinit var mainViewModel: MainViewModel
    private lateinit var bannerViewModel: BannerViewModel
    private val args by navArgs<MainFragmentArgs>()
    private var isRunning = true
    private val promotionAPI: PromotionAPI by inject()
    private val noticeAPI: NoticeAPI by inject()
    private var addressPopupShow: Boolean = true
    private lateinit var addressPopupWindow: PopupWindow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = SPUtils.getInstance("app")
        if (!pref.getBoolean("init", false)) { // 앱 최초 실행시 온보딩 화면부터
            appViewModel.appState = AppViewModel.AppState.FIRST_JOIN
            findNavController().navigate(R.id.action_mainFragment_to_onboardingFragment)
            return
        }
        if (pref.getBoolean("firstAddAddress", false)) { // 앱 최초 주소 설정 완료 후 다시 메인으로 왔을 경우
            pref.remove("firstAddAddress")
            val popupView: View = layoutInflater.inflate(R.layout.popup_address_check, null)
            addressPopupWindow = PopupWindow(
                popupView,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                isFocusable = false
                isOutsideTouchable = false
                showAtLocation(popupView, Gravity.TOP, 0, 0)
            }
            popupView.setOnClickListener {
                addressPopupWindow.dismiss()
            }
        }

        if (args.joined && !appViewModel.showJoinPopup) { // 회원가입 완료했을 경우
            appViewModel.showJoinPopup = true
            showMessageDialog(
                "회원가입이 완료되었습니다!", "동백통 카카오톡 채널을 추가하고\n" +
                        "다양한 소식과 이벤트 정보를\n" +
                        "받아보시겠어요?",
                showWarningImageView = true,
                showCancelButton = true
            ) {
                onDoneButtonClick {
                    try {
                        val url =
                            TalkApiClient.instance.channelChatUrl(getString(R.string.kakaoOpenId))
                        KakaoCustomTabsClient.openWithDefault(this.context, url)
                    } catch (t: Throwable) {
                        view.showMessageBar("카카오톡 앱 설치 후 다시 이용해주세요.")
                        t.printStackTrace()
                    }
                    dismiss()
                }
            }
        }
        with(binding) {
            deliveryAddressViewModel.observeSelectedAddress(requireActivity()) {
                addressTitleTextView.text = it?.road?.ifEmpty { it.jibun } ?: "주소 등록하기"
            }
            initViewModel()
            initClickListener()
            showEventPopup()
            initEventBanner()
            initFixedNotice()
            activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
                AppUtils.exitApp()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.goToShopRegistView -> com.theone.busandbt.utils.AppUtils.openWebsite("https://ceo.builplan.com/v2/application")

            R.id.goToShoppingImageView -> {
                binding.goToShoppingImageView.navigate(
                    R.id.service_main_graph, ServiceMainFragmentArgs(
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }

            R.id.searchEditText -> findNavController().navigate(R.id.action_mainFragment_to_searchMainFragment)
            R.id.addressTitleTextView -> findNavController().navigate(R.id.action_mainFragment_to_haveAddressSetFragment)
            R.id.goToMyInfoTextView -> {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return
                }
                view?.navigate(R.id.myinfo)
            }

            R.id.goToJjimTextView -> {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return
                }
                findNavController().navigate(R.id.action_mainFragment_to_myWishListFragment)
            }

            R.id.goToInstantButton -> findNavController().navigate(R.id.action_mainFragment_to_restaurantMainFragment)
            R.id.goToHistoryOrderTextView -> {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return
                }
                findNavController().navigate(R.id.action_mainFragment_to_orderHistoryFragment)
            }

            R.id.bannerCard -> findNavController().navigate(R.id.action_mainFragment_to_promotionListFragment)
            R.id.goToFixedNoticeView -> {
                val current = mainViewModel.currentFixedNotice ?: return
                val action =
                    MainFragmentDirections.actionMainFragmentToNoticeContentFragment(current.id)
                findNavController().navigate(action)
            }

            R.id.goToPackingImageView -> {
                findNavController().navigate(
                    R.id.service_main_graph,
                    ServiceMainFragmentArgs(
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.PACKAGING.id
                    ).toBundle()
                )
            }

            R.id.goToCouponTextView -> {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return
                }
                findNavController().navigate(R.id.action_mainFragment_to_myCouponFragment)
            }

            R.id.openSourceLicenses -> ActivityUtils.startActivity(
                Intent(
                    requireActivity(),
                    OssLicensesMenuActivity::class.java
                )
            )
            R.id.terms -> com.theone.busandbt.utils.AppUtils.openWebsite("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/builplanService.html")
            R.id.infoPolicy -> com.theone.busandbt.utils.AppUtils.openWebsite("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/builplanPrivacy.html")
            R.id.businessInfo -> view?.navigate(R.id.business_info_graph)
        }
    }

    private fun initFixedNotice() {
        safeApiRequest(
            noticeAPI.getFixedNoticeList()
        ) {
            mainViewModel.setFixedNoticeList(it)
            if (it.isEmpty()) return@safeApiRequest
            with(binding) {
                fixedNoticeTitleView.text = it.first().title
                fixedNoticeTitleView.visibility = View.VISIBLE
                fixedNoticeImageView.visibility = View.VISIBLE
                mainViewModel.setCurrentFixedNotice(0)
            }
            if (it.size < 2) return@safeApiRequest
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    var position = 1 // 첫 공지는 이미 설정이 됐기 때문
                    while (true) {
                        delay(3000L)
                        val fixedNoticeList = mainViewModel.fixedNoticeList ?: continue
                        launch(Dispatchers.Main) {
                            mainViewModel.setCurrentFixedNotice(position)
                        }.join()
                        if (position == fixedNoticeList.size - 1) position = 0 else position++
                    }
                }
            }
        }
    }

    private fun initWindow() {
        // 위에 시스템바 색변경
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        WindowInsetsControllerCompat(window, requireView()).isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.mainColor)
    }

    private fun initViewModel() {
        bannerViewModel = ViewModelProvider(this)[BannerViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mainViewModel.observeCurrentFixedNotice(this) {
            with(binding) {
                if (it != null) fixedNoticeTitleView.text = it.title
            }
        }
    }

    private fun initEventBanner() {
        with(binding) {
            safeApiRequest(
                promotionAPI.getPromotionList(
                    promotionType = PromotionType.BANNER.id,
                    promotionStatus = PromotionStatus.PROGRESS.id,
                    displayLocation = PromotionScreenType.MAIN_SCREEN.id
                )
            ) {
                bannerViewPager.apply {
                    clipToOutline = true
                    bannerViewPagerAdapter = BannerViewPagerAdapter().apply {
                        setOnItemClick { view, _, item ->
                            view.navigate(
                                R.id.promotionDetailFragment,
                                PromotionDetailFragmentArgs(item.id).toBundle()
                            )
                        }
                    }
                    adapter = bannerViewPagerAdapter
                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            bannerViewPager.clearFocus()
                            isRunning = true
                            pageNumberTextView.text = "${position + 1}"
                            //직접 유저가 스크롤했을 떄!
                            bannerViewModel.setCurrentPosition(position)
                        }
                    })
                }
                bannerViewModel.bannerItemList.observe(viewLifecycleOwner) { bannerItemList ->
                    bannerViewPagerAdapter.setItems(bannerItemList)
                }
                bannerViewModel.currentPosition.observe(viewLifecycleOwner) { currentPosition ->
                    // TODO - 이 로직이 실행됐을때 스크롤이 자동으로 올라가는 문제가 생겨서 임시방편으로 처리해놓음
                    if (nestedScrollView.scrollY < nestedScrollView.measuredHeight / 3) bannerViewPager.currentItem =
                        currentPosition
                }
                pageAllTextView.text = "/ ${it.size}"
                bannerViewModel.setBannerItems(it)
                registerTimerJob()
            }
        }
    }

    private fun initClickListener() {
        with(binding) {
            searchEditText.setOnClickListener(this@MainFragment) // 검색하기로 넘어가는 이벤트
            addressTitleTextView.setOnClickListener(this@MainFragment) // 주소등록하기로 넘어가는 이벤트
            goToInstantButton.setOnClickListener(this@MainFragment) // 클릭시 음식점으로 넘어가는 이벤트
            bannerCard.setOnClickListener(this@MainFragment)
            fixedNoticeTitleView.setOnClickListener(this@MainFragment)
            goToPackingImageView.setOnClickListener(this@MainFragment)
            goToShopRegistView.setOnClickListener(this@MainFragment) //입점신청
            goToFixedNoticeView.setOnClickListener(this@MainFragment)//동백통 결제방법
            goToShoppingImageView.setOnClickListener(this@MainFragment) //쇼핑몰
            openSourceLicenses.setOnClickListener(this@MainFragment)
            terms.setOnClickListener(this@MainFragment)
            infoPolicy.setOnClickListener(this@MainFragment)
            businessInfo.setOnClickListener(this@MainFragment)
            with(bottomMenuInclude) {
                goToMyInfoTextView.setOnClickListener(this@MainFragment) // my동백으로 넘어가는 이벤트
                goToHistoryOrderTextView.setOnClickListener(this@MainFragment)
                goToCouponTextView.setOnClickListener(this@MainFragment)
                goToJjimTextView.setOnClickListener(this@MainFragment)
            }
        }
    }

    /**
     * 이벤트 배너 팝업을 띄운다.
     */
    private fun showEventPopup() {
        val today = LocalDate.now()
        val pref = requireActivity().getPreferences(0)
        val editor = pref.edit()
        //Preference에 저장된 날자와 현재 날짜 확인 후 참일시 팝업 호출하지 않음
        if (pref.getString("popupDate", null) == today.toString()) return
        safeApiRequest(
            promotionAPI.getRandomPromotionPopup()
        ) { pd ->
            val ctx = view?.context ?: return@safeApiRequest
            val popupLayout =
                LayoutInflater.from(ctx)
                    .inflate(R.layout.popup_event_coupon, null)
            val builder = AlertDialog.Builder(ctx).setView(popupLayout)
            val callPopupWindow = builder.show()
            callPopupWindow.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val adImageView = popupLayout.findViewById<ImageView>(R.id.adImageView)
            val noMoreTodayBtn = popupLayout.findViewById<Button>(R.id.noMoreTodayButton)
            val cancelBtn = popupLayout.findViewById<Button>(R.id.cancelButton)
            Glide.with(ctx).load(pd.imageUrl).into(adImageView)
            //그만보기 클릭시 Preference로 날짜 저장
            noMoreTodayBtn?.setOnClickListener {
                callPopupWindow.dismiss()
                editor.putString("popupDate", today.toString()).apply()
            }
            cancelBtn?.setOnClickListener {
                callPopupWindow.dismiss()
            }
            adImageView?.setOnClickListener {
                callPopupWindow.dismiss()
                val action =
                    MainFragmentDirections.actionMainFragmentToPromotionDetailFragment(pd.landingUrl)
                findNavController().navigate(action)
            }
        }
    }

    /**
     * 시간마다 동작해야하는 작업들을 정의한다.
     */
    private fun registerTimerJob() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    while (isRunning) {
                        delay(3000)  //3초 후에 자동으로 배너가 넘어감
                        bannerViewModel.getCurrentPosition()?.let {
                            bannerViewModel.setCurrentPosition((it.plus(1)) % 3)
                        }
                    }
                }
            }
        }
    }

    // isRunning  변수를 만들어, onPause, onResume 때는 멈춤과 재실행이 가능하도록 합니다.
    override fun onPause() {
        super.onPause()
        isRunning = false
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        WindowInsetsControllerCompat(window, requireView()).isAppearanceLightStatusBars = true
        //1번 노출 후 다른 프래그먼트로 이동시 팝업 보이지 않음
        addressPopupShow = false
        // addressPopupWindow가 초기화되었는지 확인 하고 ,팝업이 현재 보여 지는 중이라면 onPause에서 dismiss처리
        if (this::addressPopupWindow.isInitialized && addressPopupWindow.isShowing) {
            addressPopupWindow.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        initWindow()
        isRunning = true
    }
}
