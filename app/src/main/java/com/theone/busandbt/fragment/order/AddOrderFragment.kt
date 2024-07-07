package com.theone.busandbt.fragment.order

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.busandbt.code.*
import com.theone.busandbt.R
import com.theone.busandbt.adapter.card.CardListAdapter
import com.theone.busandbt.adapter.coupon.UseCouponListAdapter
import com.theone.busandbt.adapter.order.AddOrderOutMenuListAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.CardAPI
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.api.payment.PaymentAPI
import com.theone.busandbt.databinding.FragmentAddOrderBinding
import com.theone.busandbt.dialog.PrivacyForAddOrderDialog
import com.theone.busandbt.dialog.RiderMemoSelectionDialog
import com.theone.busandbt.dto.ErrorResponse
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.dto.card.CardInfoListItem
import com.theone.busandbt.dto.cost.DeliveryCostMenu
import com.theone.busandbt.dto.coupon.UseCoupon
import com.theone.busandbt.dto.order.request.AddOrderRequest
import com.theone.busandbt.dto.order.request.AddOrderShopRequest
import com.theone.busandbt.dto.order.request.DongBaekGeonPaymentInfo
import com.theone.busandbt.dto.payment.request.SendOrderDongBaekGeonRequest
import com.theone.busandbt.dto.shop.BasketInfo
import com.theone.busandbt.eventbus.RemoveCardEvent
import com.theone.busandbt.eventbus.order.ReceiveDongBaekGeonPaymentEvent
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.payment.PaymentSimplePasswordFragmentArgs
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.item.basket.BasketShop
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.model.order.AddOrderViewModel
import com.theone.busandbt.utils.APP_SETTINGS_KEY
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.properties.Delegates

/**
 * 바로배달 주문하기 프래그먼트
 */
class AddOrderFragment : DataBindingFragment<FragmentAddOrderBinding>(),
    RequiredLogin, EnabledGoBackButton, EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_add_order
    override val actionBarTitle: String = "주문하기"
    private val args by navArgs<AddOrderFragmentArgs>()
    private val serviceType: ServiceType by lazy { ServiceType.find(args.serviceTypeId) }
    private val deliveryType: DeliveryType by lazy { DeliveryType.find(args.deliveryTypeId) }
    private val categoryViewModel: CategoryViewModel by activityViewModel()
    private val orderAPI: OrderAPI by inject()
    private val shopAPI: ShopAPI by inject()
    private val paymentAPI: PaymentAPI by inject()
    private val cardAPI: CardAPI by inject()
    private val couponAPI: CouponAPI by inject()
    private val packagingColor: Int by lazy {
        ColorUtils.getColor(R.color.packagingColor)
    }
    private val parcelColor: Int by lazy {
        ColorUtils.getColor(R.color.parcelColor)
    }
    private val bundleColor: Int by lazy {
        ColorUtils.getColor(R.color.bundleColor)
    }
    private val viewModel: AddOrderViewModel by viewModels()
    private val useCouponList = ArrayList<UseCoupon>()
    private var shopCouponDiscountCost: Int by Delegates.observable(0) { _, _, newValue ->
        binding.shopCouponDiscountCostTextView.text =
            getString(R.string.commonCost, newValue.toMoneyFormat())
        setTotalPaymentCost()
    }
    private var eventCouponDiscountCost: Int by Delegates.observable(0) { _, _, newValue ->
        binding.eventCouponDiscountCostTextView.text =
            getString(R.string.commonCost, newValue.toMoneyFormat())
        setTotalPaymentCost()
    }

    //    private var shopCouponCount: Int by Delegates.observable(0) { _, _, newValue ->
//        binding.shopCouponCountTextView.text = "${newValue}개 보유"
//    }
//    private var eventCouponCount: Int by Delegates.observable(0) { _, _, newValue ->
//        binding.eventCouponCountTextView.text = "${newValue}개 보유"
//    }
    private var shopCouponSelected = false
    private var eventCouponSelected = false
    private val dongbaektongText = buildSpannedString {
        append("동백전 카드 결제시 기본 5% + 추가 5%\n")
        append(
            "총 10% 캐시백 ",
            ForegroundColorSpan(MAIN_COLOR),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        append("적용")
    }
    private val siteCardText = buildSpannedString {
        append("현장에서")
        append(
            " 카드결제",
            ForegroundColorSpan(MAIN_COLOR),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    private val siteCashText = buildSpannedString {
        append("현장에서")
        append(
            " 현금결제",
            ForegroundColorSpan(MAIN_COLOR),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    private var addressRoad = ""
    private var addressDetail = ""
    private var packagingDiscountCost = 0

    override fun onPause() {
        super.onPause()
        with(binding) {
            viewModel.riderMemo = riderMemoFormInclude.riderMemoSelection.text.toString()
            viewModel.riderMemoInput = riderMemoFormInclude.riderMemoInputTextView.text.toString()
            viewModel.isSelectedRiderMemoNextUse =
                riderMemoFormInclude.riderMemoNextUseCheckBox.isChecked
            when (serviceType) {
                ServiceType.FOOD_DELIVERY -> {
                    viewModel.shopMemo = shopMemoInputTextView.text.toString()
                    viewModel.isSelectedShopMemoNextUse = shopMemoNextUseCheckBox.isChecked
                    viewModel.isSelectedExceptDisposable = exceptDisposableCheckBox.isChecked
                }

                ServiceType.SHOPPING_MALL -> {
                    viewModel.customerName = shopMemoInputTextView.text.toString()
                    viewModel.isSelectedCustomerNameNextUse = shopMemoNextUseCheckBox.isChecked
                }

                else -> {}
            }
            viewModel.selectedPaymentTypeId = when {
                cardPayToggleButton.isSelected -> PaymentType.PREPAY_CARD
                dongbaektongCardToggleButton.isSelected -> PaymentType.PREPAY_LOCAL_CURRENCY
                sitePayToggleButton.isSelected -> {
                    when {
                        sitePaymentInclude.siteCardPayCheckBox.isChecked -> PaymentType.MEET_CARD
                        sitePaymentInclude.siteCashPayCheckBox.isChecked -> PaymentType.MEET_CASH
                        else -> null
                    }
                }

                else -> null
            }?.id
            viewModel.isSelectedPaymentNextUse = paymentTypeNextUseCheckBox.isChecked
            //  viewModel.setCouponCount(shopCouponCount, eventCouponCount)
            viewModel.useCouponList.clear()
            viewModel.useCouponList.addAll(useCouponList)
            viewModel.shopCouponDiscountCost = shopCouponDiscountCost
            viewModel.eventCouponDiscountCost = eventCouponDiscountCost
            viewModel.isSelectedSecurityTelUse =
                deliveryAddressFormInclude.securityCheckBox.isChecked
            shopCouponSelected = false
            eventCouponSelected = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        val basketShopList =
            basketListViewModel.getSelectedBasketShopList(args.serviceTypeId, args.deliveryTypeId)
        if (basketShopList.isEmpty()) {
            view.showMessageBar("장바구니가 비어있습니다.")
            return
        }

        val firstBasketShop = basketShopList.first()
        binding.deliveryType = deliveryType
        if (deliveryType == DeliveryType.PACKAGING) {
            safeApiRequest(
                shopAPI.getShopInfo(firstBasketShop.shopId)
            ) {
                addressRoad = it.road
                addressDetail = it.addressDetail
            }
        }
        safeApiRequest(
            shopAPI.getShopDetail(firstBasketShop.shopId)
        ) {
            if (deliveryType == DeliveryType.PACKAGING) packagingDiscountCost =
                it.packagingDiscountCost
            val ourTown = categoryViewModel.foodDeliveryCategoryList.find { c -> c.name == "우리동네" }
            val isOurTownOrder = it.categoryList.find { c -> c.id == ourTown?.id } != null
            if (!isOurTownOrder) {
                binding.deliveryAddressFormInclude.securityTelGroup.isVisible = true
            }
        }
        viewModel.basketInfoListLiveData.observe(viewLifecycleOwner) {
            if (it != null) initView(basketShopList, it)
        }
//        viewModel.couponCountLiveData.observe(viewLifecycleOwner) {
//            initCouponCount(it)
//        }
        if (viewModel.basketInfoListLiveData.value == null) {
            binding.root.alpha = 0f
            val selectedDeliveryAddress = deliveryAddressViewModel.selectedDeliveryAddress ?: return
            val menuList = basketShopList.flatMap { it.menuList }
            safeApiRequest(
                shopAPI.getBasketInfo(
                    selectedDeliveryAddress.jibun,
                    selectedDeliveryAddress.lat,
                    selectedDeliveryAddress.lng,
                    basketShopList.map { it.shopId }.querySetParam,
                    menuList.map { it.menuId }.querySetParam,
                    menuList.flatMap { it.optionList }.map { it.optionId }.querySetParam
                )
            ) {
                viewModel.setBasketInfoList(it.shopBasketInfoList)
            }
        }

//        if (viewModel.couponCountLiveData.value == null) {
//            val shopId = basketShopList.firstOrNull()?.shopId
//            safeApiRequest(
//                couponAPI.getMemberCouponCount(
//                    innerLoginInfo.getFormedToken(),
//                    innerLoginInfo.id,
//                    serviceType = serviceType.id,
//                    deliveryType = deliveryType.id,
//                    couponType = CouponType.SHOP.id,
//                    shopId = shopId,
//                    paymentCost = getOrderCost()
//                ),
//                showProgress = false
//            ) {
//               // shopCouponCount = it.couponCount
//            }
//            safeApiRequest(
//                couponAPI.getMemberCouponCount(
//                    innerLoginInfo.getFormedToken(),
//                    innerLoginInfo.id,
//                    serviceType = serviceType.id,
//                    deliveryType = deliveryType.id,
//                    couponType = CouponType.EVENT.id,
//                    paymentCost = getOrderCost()
//                ),
//                showProgress = false
//            ) {
//               // eventCouponCount = it.couponCount
//            }
//        }
    }

    private fun receiveDongBaekGeonData(i: Intent) {
        if (i.data?.host != "payment") return
        val e = i.extras ?: return
        val request = viewModel.addOrderRequestLiveData.value ?: return
        val trNo = (e.getString("trNo") ?: "").replace(" ", "+")
        val secret = (e.getString("secret") ?: "").replace(" ", "+")
        val userId = e.getString("userId") ?: "" // 동백전 회원 ID
        request.dongBaekGeonPaymentInfo = DongBaekGeonPaymentInfo(
            userId = userId,
            trNo = trNo,
            secret = secret
        )
        actualAddOrder(request)
    }

//    private fun initCouponCount(couponCount: AddOrderCouponCount) {
//        initCouponCount(couponCount.shopCouponCount, couponCount.eventCouponCount)
//    }
//
//    private fun initCouponCount(shopCouponCount: Int, eventCouponCount: Int) {
//        this.shopCouponCount = shopCouponCount
//        this.eventCouponCount = eventCouponCount
//    }

    private fun initView(basketShopList: List<BasketShop>, basketInfoList: List<BasketInfo>) {
        val innerLoginInfo = loginInfo ?: return
        val basketShop = basketShopList.firstOrNull() ?: return
        val basketInfo = basketInfoList.find { it.shopId == basketShop.shopId } ?: return
        with(binding) {
            dongbaektongPaymentInclude.benefitsSubTextView.text = dongbaektongText
            privacyAgreeCheckBox.setOnCheckedChangeListener { _, b ->
                addOrderButton.isEnabled = b
            }
            binding.privacyAgreeDetailButton.setOnClickListener {
                PrivacyForAddOrderDialog(basketShop.shopName).show(childFragmentManager, null)
            }

            val sp = SPUtils.getInstance("addOrder")

            val viewCachedIsSelectedSecurityTelUse = viewModel.isSelectedSecurityTelUse
            if (viewCachedIsSelectedSecurityTelUse == null) {
                deliveryAddressFormInclude.securityCheckBox.isChecked =
                    SPUtils.getInstance(APP_SETTINGS_KEY).getBoolean("useSecurityTel", true)
            } else {
                deliveryAddressFormInclude.securityCheckBox.isChecked =
                    viewCachedIsSelectedSecurityTelUse
            }

            when (serviceType) {
                ServiceType.FOOD_DELIVERY -> {
                    val viewCachedShopMemo = viewModel.shopMemo
                    if (viewCachedShopMemo == null) {
                        val defaultShopMemo = sp.getString("shopMemo")
                        if (defaultShopMemo.isNotEmpty()) {
                            shopMemoNextUseCheckBox.isChecked = true
                            shopMemoInputTextView.setText(defaultShopMemo)
                        }
                    } else {
                        shopMemoInputTextView.setText(viewCachedShopMemo)
                        shopMemoNextUseCheckBox.isChecked =
                            viewModel.isSelectedShopMemoNextUse ?: false
                    }
                    exceptDisposableCheckBox.isChecked = viewModel.isSelectedExceptDisposable
                }

                ServiceType.SHOPPING_MALL -> {
                    val viewCachedCustomerName = viewModel.customerName
                    if (viewCachedCustomerName == null) {
                        val defaultCustomerName = sp.getString("customerName")
                        if (defaultCustomerName.isNotEmpty()) {
                            shopMemoNextUseCheckBox.isChecked = true
                            shopMemoInputTextView.setText(defaultCustomerName)
                        }
                    } else {
                        shopMemoInputTextView.setText(viewCachedCustomerName)
                        shopMemoNextUseCheckBox.isChecked =
                            viewModel.isSelectedCustomerNameNextUse ?: false
                    }
                }

                else -> {}
            }

            if (!shopCouponSelected) {
                shopCouponDiscountCost = viewModel.shopCouponDiscountCost ?: 0
            }
            if (!eventCouponSelected) {
                eventCouponDiscountCost = viewModel.eventCouponDiscountCost ?: 0
            }

            loginInfoViewModel.loginInfoLiveData.observe(viewLifecycleOwner) {
                if (it != null) customerTel = it.tel
            }

            val typeface = ResourcesCompat.getFont(requireContext(), R.font.sult_medium)
            // 배달기사 요청사항 캐시
            if (args.deliveryTypeId in arrayOf(DeliveryType.INSTANT.id, DeliveryType.BUNDLE.id)) {
                val viewCachedRiderMemo = viewModel.riderMemo
                if (viewCachedRiderMemo == null) { // viewModel 캐시가 없을 경우에는 sp에 저장해둔 값을 사용한다.
                    val defaultRiderMemoSelection = sp.getString("riderMemoSelection")
                    if (defaultRiderMemoSelection.isNotEmpty()) {
                        riderMemoFormInclude.riderMemoNextUseCheckBox.isChecked = true
                        riderMemoFormInclude.riderMemoSelection.text = defaultRiderMemoSelection
                        riderMemoFormInclude.riderMemoSelection.typeface = typeface
                        val flag = defaultRiderMemoSelection == "직접 입력"
                        riderMemoFormInclude.riderMemoInputTextView.isVisible = flag
                        if (flag) {
                            val defaultRiderMemo = sp.getString("riderMemo")
                            riderMemoFormInclude.riderMemoInputTextView.setText(
                                defaultRiderMemo ?: ""
                            )
                        }
                    }
                } else {
                    riderMemoFormInclude.riderMemoSelection.text = viewCachedRiderMemo
                    riderMemoFormInclude.riderMemoSelection.typeface = typeface
                    riderMemoFormInclude.riderMemoNextUseCheckBox.isChecked =
                        viewModel.isSelectedRiderMemoNextUse ?: false
                    val flag = viewCachedRiderMemo == "직접 입력"
                    riderMemoFormInclude.riderMemoInputTextView.isVisible = flag
                    if (flag) riderMemoFormInclude.riderMemoInputTextView.setText(
                        viewModel.riderMemoInput ?: ""
                    )
                }
            }

            val viewCachedIsSelectedPaymentNextUse = viewModel.isSelectedPaymentNextUse
            if (viewCachedIsSelectedPaymentNextUse == null) {
                if (sp.contains("paymentType")) paymentTypeNextUseCheckBox.isChecked = true
            } else paymentTypeNextUseCheckBox.isChecked = viewCachedIsSelectedPaymentNextUse

            deliveryAddress = deliveryAddressViewModel.selectedDeliveryAddress
            customerTel = innerLoginInfo.tel

            when (args.deliveryTypeId) {
                DeliveryType.PACKAGING.id -> {
                    deliveryAddressFormInclude.root.visibility = View.GONE // 배달지
                    riderMemoFormInclude.root.visibility = View.GONE // 배달기사 요청사항
                    deliveryTypeImageView.setImageResource(R.drawable.ic_add_order_packaging)
                }

                DeliveryType.BUNDLE.id -> {
                    deliveryTypeImageView.setImageResource(R.drawable.ic_add_order_packaging)
                }

                DeliveryType.PARCEL.id -> {
                    shopMemoTitle.text = "배송 받으실 분"
                    shopMemoInputTextView.hint = "이름을 입력해 주세요."
                    riderMemoFormInclude.riderMemoTitle.text = " 배송 요청사항"
                    exceptDisposableCheckBox.isVisible = false
                    exceptDisposableTextView.isVisible = false
                    sitePaymentInclude.root.isVisible = false
                    with(paymentFormInclude) {
                        deliveryCostTitle.text = "배송비"
                    }
                }

                else -> {}
            }
            deliveryTypeGroup.isVisible = true
            when (deliveryType) {
                DeliveryType.PARCEL -> { // 배송비를 표시해줘야 하므로 조회가 필요하다.
                    val basketInfoMap = basketInfoList.associateBy { it.shopId }
                    basketShopList.forEach {
                        val bi = basketInfoMap[it.shopId] ?: return@forEach
                        it.deliveryCost =
                            bi.deliveryCostList.findDeliveryCost(it.menuList.calculateMenuCost())
                    }
                    menuListRecyclerView.adapter =
                        AddOrderOutMenuListAdapter(basketShopList, serviceType)
                }

                else -> menuListRecyclerView.adapter =
                    AddOrderOutMenuListAdapter(basketShopList, serviceType)
            }
            menuListRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(16F)))
            setTotalPaymentCost()

            val useCouponListAdapter = UseCouponListAdapter(useCouponList)
            paymentFormInclude.useCouponRecyclerView.adapter = useCouponListAdapter

            // 쿠폰 선택 결과
            setFragmentResultListener(
                "selectOrderCoupon"
            ) { _, bundle ->
                val ucl = bundle.getParcelableArrayCompat<UseCoupon>("useCouponList")
                val totalDiscountCost = ucl.sumOf { it.couponDiscountCost }
                when (bundle.getInt("couponTypeId")) {
                    CouponType.SHOP.id -> {
                        useCouponList.removeAll { it.shopId > 0 }
                        useCouponList.addAll(ucl)
                        useCouponListAdapter.setItems(useCouponList)
                        shopCouponDiscountCost = totalDiscountCost
                        shopCouponSelected = true
                    }

                    CouponType.EVENT.id -> {
                        useCouponList.removeAll { it.shopId == 0 }
                        useCouponList.addAll(ucl)
                        useCouponListAdapter.setItems(useCouponList)
                        eventCouponDiscountCost = totalDiscountCost
                        eventCouponSelected = true
                    }
                }
            }
            childFragmentManager.setFragmentResultListener(
                "request",
                this@AddOrderFragment
            ) { _, bundle ->
                val resultReceived = bundle.getString("requestText")
                riderMemoFormInclude.riderMemoSelection.text = resultReceived
                riderMemoFormInclude.riderMemoSelection.typeface = typeface
                riderMemoFormInclude.riderMemoInputTextView.isVisible = resultReceived == "직접 입력"
            }

            sitePaymentInclude.siteCardPay.text = siteCardText
            sitePaymentInclude.siteCashPay.text = siteCashText

            //포커스 됐을 때, 안 됐을 때 폰트 바꾸기 , 백그라운드 변경
            riderMemoFormInclude.riderMemoInputTextView.setOnFocusChangeListener { view, b ->
                view.setBackgroundResource(if (b) R.drawable.bg_address_edittext_selector else R.drawable.bg_card_text_background)
            }
            deliveryAddressFormInclude.addressDetailEditText.setOnFocusChangeListener { view, b ->
                view.setBackgroundResource(if (b) R.drawable.bg_address_edittext_selector else R.drawable.bg_card_text_background)
            }
            initClickListener()
            changeFont(deliveryAddressFormInclude.addressDetailEditText)
            changeFont(riderMemoFormInclude.riderMemoInputTextView)
            initDeliveryTypeView()

            val activeDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_active)
            val inactiveDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_indicator_inactive)
            safeApiRequest(
                cardAPI.getCardList(
                    innerLoginInfo.getFormedToken(),
                    innerLoginInfo.id
                )
            ) {
                val modifiedList = mutableListOf<CardInfoListItem>()
                modifiedList.addAll(it)
                modifiedList.add(CardInfoListItem(0, 0, "0"))
                modifiedList.forEach { _ ->
                    cardPaymentInclude.indicatorContainer.addView(createDotView(inactiveDrawable))
                }
                cardPaymentInclude.cardList.adapter = CardListAdapter().apply {
                    addItems(modifiedList)
                    setOnItemClick { _, _, item ->
                        if (item.type == 0) findNavController().navigate(R.id.card_agree_graph)
                    }
                    val snapHelper = PagerSnapHelper()
                    snapHelper.attachToRecyclerView(cardPaymentInclude.cardList)
                }
            }

            cardPaymentInclude.cardList.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val activePosition =
                        (cardPaymentInclude.cardList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    for (i in 0 until cardPaymentInclude.indicatorContainer.childCount) {
                        val dot = cardPaymentInclude.indicatorContainer.getChildAt(i)
                        dot.background =
                            if (i == activePosition) activeDrawable else inactiveDrawable
                    }
                }
            })

            initPaymentTypeForm(basketInfo.paymentTypeList)
            root.playAlphaAnimation()
        }
    }

    /**
     * 주문하기 결제유형 뷰를 초기화한다.
     */
    private fun initPaymentTypeForm(paymentTypeList: List<Int>) {
        val sp = SPUtils.getInstance("addOrder")
        val d = this@AddOrderFragment.deliveryType
        with(binding) {
            dongbaektongCardToggleButton.setOnCheckedChangeListener { _, isChecked ->
                dongbaektongPaymentInclude.root.isVisible = isChecked
            }

            cardPayToggleButton.setOnCheckedChangeListener { _, isChecked ->
                cardPaymentInclude.root.isVisible = isChecked
            }

            sitePayToggleButton.setOnCheckedChangeListener { _, isChecked ->
                sitePaymentInclude.root.isVisible = isChecked
            }

            // 결제수단 캐시
            val paymentType = viewModel.selectedPaymentTypeId ?: sp.getInt("paymentType", 0)
            if (paymentType != 0 && paymentType in paymentTypeList) {
                when (PaymentType.find(paymentType)) {
                    PaymentType.PREPAY_LOCAL_CURRENCY -> selectPayment(PaymentType.PREPAY_LOCAL_CURRENCY)
                    PaymentType.PREPAY_CARD -> selectPayment(PaymentType.PREPAY_CARD)
                    PaymentType.MEET_CARD -> {
                        if (d != DeliveryType.PARCEL) {
                            selectPayment(PaymentType.MEET_CARD)
                        }
                    }

                    PaymentType.MEET_CASH -> {
                        if (d != DeliveryType.PARCEL) {
                            selectPayment(PaymentType.MEET_CASH)
                        }
                    }

                    else -> {}
                }
            }

            val isShowDongbaekGeonPaymentForm =
                paymentTypeList.contains(PaymentType.PREPAY_LOCAL_CURRENCY.id)
            dongbaekgeonGroup.isVisible = isShowDongbaekGeonPaymentForm
            if (!isShowDongbaekGeonPaymentForm && dongbaektongCardToggleButton.isChecked) dongbaektongCardToggleButton.isChecked =
                false

            val isShowCardPaymentForm = paymentTypeList.contains(PaymentType.PREPAY_CARD.id)
            cardPayToggleButton.isVisible = isShowCardPaymentForm
            if (!isShowCardPaymentForm && cardPayToggleButton.isChecked) cardPayToggleButton.isChecked =
                false

            val isShowMeetCardPaymentForm = paymentTypeList.contains(PaymentType.MEET_CARD.id)
            sitePaymentInclude.siteCardPay.isVisible = isShowMeetCardPaymentForm
            sitePaymentInclude.siteCardPayCheckBox.isVisible = isShowMeetCardPaymentForm
            if (!isShowMeetCardPaymentForm) {
                // 선택되어 있을 경우 선택해제
                if (sitePaymentInclude.siteCardPayCheckBox.isChecked) sitePaymentInclude.siteCardPayCheckBox.isChecked =
                    false
                sitePaymentInclude.siteCashPayCheckBox.setMargin(top = 0)
                sitePaymentInclude.siteCashPay.setMargin(top = 0)
            }

            val isShowMeetCashPaymentForm = paymentTypeList.contains(PaymentType.MEET_CASH.id)
            sitePaymentInclude.siteCashPay.isVisible = isShowMeetCashPaymentForm
            sitePaymentInclude.siteCashPayCheckBox.isVisible = isShowMeetCashPaymentForm
            if (!isShowMeetCashPaymentForm) {
                if (sitePaymentInclude.siteCashPayCheckBox.isChecked) sitePaymentInclude.siteCashPayCheckBox.isChecked =
                    false
            }

            if (!isShowMeetCardPaymentForm && !isShowMeetCashPaymentForm) {
                if (sitePayToggleButton.isChecked) sitePayToggleButton.isChecked = true
                sitePayToggleButton.isVisible = false
            }

            cardPayToggleButton.setOnClickListener {
                selectPayment(PaymentType.PREPAY_CARD)
            }
            dongbaektongCardToggleButton.setOnClickListener {
                selectPayment(PaymentType.PREPAY_LOCAL_CURRENCY)
            }
            sitePayToggleButton.setOnClickListener {
                selectPayment(PaymentType.MEET_CARD)
            }
        }
    }

    private fun initClickListener() {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        val basketShopList =
            basketListViewModel.getSelectedBasketShopList(args.serviceTypeId, args.deliveryTypeId)
        with(binding) {

            deliveryAddressFormInclude.phonePopupTextView.setOnClickListener {
                showMessageDialog(
                    "안심번호는 개인정보보호를 위해\n" +
                            "연락처가 유출되지 않게\n" +
                            "실제 전화번호 대신 발급되는 임시번호입니다.\n" +
                            "발급된 안심번호는 일정 시간\n" +
                            "이후 자동으로 해지됩니다.",
                    showWarningImageView = false,
                    showCancelButton = true
                ) {
                    onDoneButtonClick { dismiss() }
                }
            }

            riderMemoFormInclude.riderMemoSelection.setOnClickListener {
                RiderMemoSelectionDialog(riderMemoFormInclude.riderMemoSelection.text).show(
                    childFragmentManager,
                    null
                )
            }

            shopSeeAddressTextView.setOnClickListener {
                showMessageDialog(
                    addressRoad + addressDetail,
                    showWarningImageView = false,
                    showCancelButton = true
                ) {
                    onDoneButtonClick(buttonText = "복사하기") {
                        ClipboardUtils.copyText(addressRoad + addressDetail)
                        dismiss()
                    }
                }
            }
            shopCouponTextView.setOnClickListener {
                val selectedShopCouponIds =
                    useCouponList.filter { it.type == CouponType.EVENT.id }.map { it.couponId }
                        .toIntArray()
                val action =
                    AddOrderFragmentDirections.actionAddOrderFragmentToCouponListFragment(
                        basketShopList.map { bs -> bs.shopId }.toIntArray(),
                        CouponType.SHOP.id,
                        args.serviceTypeId,
                        args.deliveryTypeId,
                        getOrderCost(),
                        selectedShopCouponIds
                    )
                findNavController().navigate(action)
            }
            eventCoupon.setOnClickListener {
                val selectedEventCouponIds =
                    useCouponList.filter { it.type == CouponType.SHOP.id }.map { it.couponId }
                        .toIntArray()
                val action =
                    AddOrderFragmentDirections.actionAddOrderFragmentToCouponListFragment(
                        null,
                        CouponType.EVENT.id,
                        args.serviceTypeId,
                        args.deliveryTypeId,
                        getOrderCost(),
                        selectedEventCouponIds
                    )
                findNavController().navigate(action)
            }
            addOrderButton.setDebounceClickListener {
                val pt = when {
                    cardPayToggleButton.isSelected -> PaymentType.PREPAY_CARD
                    dongbaektongCardToggleButton.isSelected -> PaymentType.PREPAY_LOCAL_CURRENCY
                    sitePayToggleButton.isSelected -> {
                        when {
                            sitePaymentInclude.siteCardPayCheckBox.isChecked -> PaymentType.MEET_CARD
                            sitePaymentInclude.siteCashPayCheckBox.isChecked -> PaymentType.MEET_CASH
                            else -> {
                                addOrderButton.showMessageBar("결제방법을 선택해야합니다.")
                                return@setDebounceClickListener
                            }
                        }
                    }

                    else -> {
                        addOrderButton.showMessageBar("결제방법을 선택해야합니다.")
                        return@setDebounceClickListener
                    }
                }

                tryAddOrder(basketShopList, da, pt)
            }
        }
    }

    private fun tryAddOrder(
        basketShopList: List<BasketShop>,
        da: DeliveryAddress,
        pt: PaymentType
    ) {
        if (basketShopList.isEmpty()) return
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            val riderMemo =
                if (riderMemoFormInclude.riderMemoInputTextView.isVisible) riderMemoFormInclude.riderMemoInputTextView.text.toString() else riderMemoFormInclude.riderMemoSelection.text.toString()
            val addOrderSp = SPUtils.getInstance("addOrder")
            if (paymentTypeNextUseCheckBox.isChecked) addOrderSp.put("paymentType", pt.id)
            else addOrderSp.remove("paymentType")
            if (shopMemoNextUseCheckBox.isChecked) addOrderSp.put(
                "shopMemo",
                shopMemoInputTextView.text.toString()
            )
            else addOrderSp.remove("shopMemo")
            if (riderMemoFormInclude.riderMemoNextUseCheckBox.isChecked) {
                addOrderSp.put(
                    "riderMemoSelection",
                    riderMemoFormInclude.riderMemoSelection.text.toString()
                )
                addOrderSp.put(
                    "riderMemo",
                    riderMemoFormInclude.riderMemoInputTextView.text.toString()
                )
            } else {
                addOrderSp.remove("riderMemoSelection")
                addOrderSp.remove("riderMemo")
            }
            val useSecurityTel =
                deliveryAddressFormInclude.securityCheckBox.takeIf { it.isVisible }?.isChecked
                    ?: false
            addOrderSp.put("useSecurityTel", useSecurityTel)

            val disposableText = if (exceptDisposableCheckBox.isChecked) "[수저포크X]" else "[수저포크O]"
            val orderShopList = basketShopList.map {
                val orderMenuList = it.getSelectedMenuList().map { menu ->
                    DeliveryCostMenu(
                        menu.menuId,
                        menu.count,
                        menu.optionList.map { option -> option.optionId },
                        menu.menuCostId
                    )
                }
                val couponIdList =
                    useCouponList
                        .map { useCoupon -> useCoupon.couponId }
                AddOrderShopRequest(it.shopId, couponIdList, orderMenuList)
            }

            val sp = SPUtils.getInstance(APP_SETTINGS_KEY)

            val request = AddOrderRequest(
                memberId = innerLoginInfo.id,
                serviceType = serviceType.id,
                deliveryType = this@AddOrderFragment.deliveryType.id,
                customerName = if (this@AddOrderFragment.deliveryType == DeliveryType.PARCEL) shopMemoInputTextView.text.toString() else null,
                customerTel = innerLoginInfo.tel,
                jibun = da.jibun,
                road = da.road,
                addressDetail = da.addressDetail,
                customerLat = da.lat,
                customerLng = da.lng,
                paymentType = pt.id,
                shopMemo = if (this@AddOrderFragment.deliveryType != DeliveryType.PARCEL) disposableText + shopMemoInputTextView.text.toString() else "",
                riderMemo = riderMemo,
                orderShopList = orderShopList,
                useSecurityTel = useSecurityTel,
                doSendOrderInfo = sp.getBoolean("sendOrderCount")
            )
            val firstBasketShop = basketShopList.first()
            val token = innerLoginInfo.getFormedToken()
            when (pt) {
                PaymentType.PREPAY_LOCAL_CURRENCY -> { // 동백전
                    val paymentCost = getTotalPaymentCost()
                    if (paymentCost > 0) {
                        safeApiRequest(
                            orderAPI.checkCanOrder(token, request),
                            showFailMessage = true
                        ) {
                            if (!it.testResult) {
                                showMessageDialog(
                                    title = it.testMessage,
                                    showWarningImageView = true
                                )
                                return@safeApiRequest
                            }
                            safeApiRequest(
                                orderAPI.createOrderId(
                                    token,
                                    AppType.BUSAN.id,
                                    serviceType.id,
                                    this@AddOrderFragment.deliveryType.id
                                )
                            ) { createOrderIdResponse ->
                                request.orderId = createOrderIdResponse.orderId
                                safeApiRequest(
                                    paymentAPI.sendOrder(
                                        SendOrderDongBaekGeonRequest(
                                            createOrderIdResponse.orderId,
                                            firstBasketShop.getSelectedMenuList().simpleDescription,
                                            paymentCost,
                                            shopId = firstBasketShop.shopId
                                        )
                                    )
                                ) { sendOrderResponse ->
                                    viewModel.setAddOrderRequest(request)
                                    ActivityUtils.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            Uri.parse("busanmoney://payment?trNo=${sendOrderResponse.trNo}&secret=${sendOrderResponse.secret}&partnerNo=0000000361&frchNo=${sendOrderResponse.frchNo}&retUrlScheme=dbt://payment&frchNm=${firstBasketShop.shopName}&authAmt=$paymentCost")
                                    })
                                }
                            }
                        }
                    } else {
                        actualAddOrder(request)
                    }
                    return@with
                }

                PaymentType.PREPAY_CARD -> {
                    val activePosition =
                        (cardPaymentInclude.cardList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val adapter = cardPaymentInclude.cardList.adapter as CardListAdapter
                    val cardInfo = adapter.items[activePosition]
                    if (cardInfo.id == 0) {
                        showMessageDialog(
                            "카드가 선택되지 않았습니다.",
                            "결제할 카드를 선택 후 주문이 가능합니다.",
                            showWarningImageView = true
                        )
                        return
                    }
                    request.cardInfoId = cardInfo.id
                    view?.navigate(
                        R.id.payment_simple_password_graph,
                        PaymentSimplePasswordFragmentArgs(request).toBundle()
                    )
                    return@with
                }

                PaymentType.MEET_CASH, PaymentType.MEET_CARD -> actualAddOrder(request)

                else -> {}
            }
        }
    }

    private fun actualAddOrder(request: AddOrderRequest) {
        val innerLoginInfo = loginInfo ?: return
        val basketShopList =
            basketListViewModel.getSelectedBasketShopList(args.serviceTypeId, args.deliveryTypeId)
        safeApiRequest(
            orderAPI.addOrder(
                innerLoginInfo.getFormedToken(),
                request
            ),
            onFail = { _, rawData ->
                if (rawData == null) return@safeApiRequest
                try {
                    val response =
                        JACKSON_OBJECT_MAPPER.readValue(rawData, ErrorResponse::class.java)
                    showMessageDialog(response.message, showWarningImageView = true)
                } catch (_: Throwable) {
                }
            }
        ) { response ->
            basketShopList.forEach { basketListViewModel.removeSelectedMenu(it) }
            val couponIdList = request.orderShopList.flatMap { it.useCouponIdList }
            innerLoginInfo.couponCount -= couponIdList.size
            loginInfoViewModel.update()
            val controller = findNavController()
            val dest = controller.currentDestination ?: return@safeApiRequest
            controller.popBackStack(R.id.basket_graph, true)
            controller.navigate(
                R.id.order_list_graph,
                OrderListMainFragmentArgs(
                    serviceType.id,
                    this@AddOrderFragment.deliveryType.id
                ).toBundle(),
                NavOptions.Builder().setPopUpTo(dest.id, false).build()
            )
            if (serviceType != ServiceType.SHOPPING_MALL) {
                controller.navigate(
                    R.id.order_status_graph,
                    OrderStatusFragmentArgs(response.orderId).toBundle(),
                    NavOptions.Builder().setPopUpTo(dest.id, false).build()
                )
            }
        }
    }

    /**
     * text크기가 0이 아닐때 폰트, 커서 위치 재설정
     */
    private fun changeFont(place: EditText) {
        place.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(frontNum: Editable?) {
                place.typeface =
                    ResourcesCompat.getFont(
                        Utils.getApp(),
                        if (place.text.isEmpty()) R.font.sult_bold else R.font.sult_regular
                    )
            }
        })
    }

    private fun initDeliveryTypeView() {
        when (deliveryType) {
            DeliveryType.INSTANT -> {
                setDeliveryTypeView(
                    R.drawable.ic_airplane_main,
                    "배달",
                    "입력하신 주소지로 상품이 배송됩니다",
                    MAIN_COLOR,
                    R.drawable.bg_add_order_instant
                )
            }

            DeliveryType.PACKAGING -> {
                setDeliveryTypeView(
                    R.drawable.ic_add_order_packaging,
                    "포장",
                    "메뉴가 준비되면 매장으로 방문해 주세요",
                    packagingColor,
                    R.drawable.bg_add_order_packaging
                )
            }

            DeliveryType.BUNDLE -> {
                setDeliveryTypeView(
                    R.drawable.ic_add_order_packaging,
                    "묶음배달",
                    "입력하신 주소지로 상품이 배송됩니다",
                    bundleColor,
                    R.drawable.bg_add_order_packaging
                )
            }

            DeliveryType.PARCEL -> {
                setDeliveryTypeView(
                    R.drawable.ic_add_order_parcel,
                    "택배배송",
                    "입력하신 주소지로 상품이 배송됩니다",
                    parcelColor,
                    R.drawable.bg_add_order_parcel
                )
            }

            else -> {}
        }
    }

    private fun getTotalPaymentCost(): Int {
        val bs = basketListViewModel.getSelectedBasketShop(args.serviceTypeId, args.deliveryTypeId)
            ?: error("비정상적인 접근입니다.")
        val menuCost = bs.getSelectedMenuList().calculateMenuCost()
        val discountCost = shopCouponDiscountCost + eventCouponDiscountCost
        val deliveryCost =
            if (args.deliveryTypeId == DeliveryType.PACKAGING.id) 0 else args.deliveryCost
        return (menuCost + deliveryCost - discountCost - packagingDiscountCost).takeIf { it >= 0 }
            ?: 0
    }

    private fun getOrderCost(): Int {
        val bs = basketListViewModel.getSelectedBasketShop(args.serviceTypeId, args.deliveryTypeId)
            ?: error("비정상적인 접근입니다.")
        return bs.getSelectedMenuList().calculateMenuCost()
    }

    private fun setTotalPaymentCost() {
        val bs = basketListViewModel.getSelectedBasketShop(args.serviceTypeId, args.deliveryTypeId)
            ?: return
        with(binding) {
            val menuCost = bs.getSelectedMenuList().calculateMenuCost()
            val menuCostText = getString(
                R.string.commonCost,
                menuCost.toMoneyFormat()
            )
            if (args.deliveryTypeId != DeliveryType.PACKAGING.id) {
                paymentFormInclude.deliveryCostTextView.text = args.deliveryCost.toCommonMoneyForm()
            }
            paymentFormInclude.orderCostTextView.text = menuCostText
            val discountCost = shopCouponDiscountCost + eventCouponDiscountCost
            val deliveryCost =
                if (args.deliveryTypeId == DeliveryType.PACKAGING.id) 0 else args.deliveryCost
            val paymentCostText = getString(
                R.string.commonCost,
                (menuCost + deliveryCost - discountCost - packagingDiscountCost).positive()
                    .toMoneyFormat()
            )
            paymentFormInclude.couponDiscountCostTextView.text =
                "(-)${discountCost.toCommonMoneyForm()}"
            if (args.deliveryTypeId == DeliveryType.PACKAGING.id) {
                paymentFormInclude.packagingDiscountCostTextView.text =
                    "(-)${packagingDiscountCost.toCommonMoneyForm()}"
            }
            paymentCostTextView.text = paymentCostText
            addOrderButton.text = "총 $paymentCostText 결제하기"
        }
    }

    private fun setDeliveryTypeView(
        @DrawableRes icon: Int,
        title: String,
        desc: String,
        titleColor: Int,
        @DrawableRes backgroundResId: Int
    ) {
        with(binding) {
            deliveryTypeImageView.setImageResource(icon)
            deliveryTypeTextView.text = title
            deliveryTypeTextView.setTextColor(titleColor)
            deliveryTypeBackgroundView.setBackgroundResource(backgroundResId)
            deliveryTypeDescTextView.setTextColor(titleColor)
            deliveryTypeDescTextView.text = desc
        }
    }

    private fun createDotView(backgroundDrawable: Drawable?): View {
        val dot = View(context)
        dot.background = backgroundDrawable
        dot.layoutParams =
            LinearLayout.LayoutParams(SizeUtils.dp2px(8F), SizeUtils.dp2px(8F)).apply {
                setMargins(SizeUtils.dp2px(8F), 0, 0, 0)
            }
        return dot
    }

    private fun resetPaymentButtons() {
        with(binding) {
            dongbaektongCardToggleButton.isChecked = false
            cardPayToggleButton.isChecked = false
            sitePayToggleButton.isChecked = false
            dongbaektongCardToggleButton.isSelected = false
            cardPayToggleButton.isSelected = false
            sitePayToggleButton.isSelected = false
        }
    }

    private fun selectPayment(paymentType: PaymentType) {
        resetPaymentButtons()
        with(binding) {
            when (paymentType) {
                PaymentType.PREPAY_CARD -> {
                    cardPayToggleButton.isSelected = true
                    cardPayToggleButton.isChecked = true
                }

                PaymentType.PREPAY_LOCAL_CURRENCY -> {
                    dongbaektongCardToggleButton.isSelected = true
                    dongbaektongCardToggleButton.isChecked = true
                }

                PaymentType.MEET_CASH, PaymentType.MEET_CARD -> {
                    sitePayToggleButton.isSelected = true
                    sitePayToggleButton.isChecked = true
                    when (paymentType) {
                        PaymentType.MEET_CASH -> {
                            if (isCheckedAnySitePayment()) return
                            sitePaymentInclude.siteCashPayCheckBox.isSelected = true
                            sitePaymentInclude.siteCashPayCheckBox.isChecked = true
                        }

                        PaymentType.MEET_CARD -> {
                            if (isCheckedAnySitePayment()) return
                            sitePaymentInclude.siteCardPayCheckBox.isSelected = true
                            sitePaymentInclude.siteCardPayCheckBox.isChecked = true
                        }

                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }

    private fun isCheckedAnySitePayment(): Boolean {
        with(binding.sitePaymentInclude) {
            if (siteCardPayCheckBox.isChecked || siteCashPayCheckBox.isChecked) return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        binding.addOrderButton.isEnabled = binding.privacyAgreeCheckBox.isChecked
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onReceiveDongBaekGeonPayment(event: ReceiveDongBaekGeonPaymentEvent) {
        receiveDongBaekGeonData(event.intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun removeCard(event: RemoveCardEvent) {
        with(binding) {
            showMessageDialog(
                "선택하신 카드를 삭제하시겠어요?",
                event.card.cardNo,
                showWarningImageView = false,
                showCancelButton = true
            ) {
                onDoneButtonClick {
                    val innerLoginInfo = loginInfo ?: return@onDoneButtonClick
                    safeApiRequest(
                        cardAPI.removeCard(innerLoginInfo.getFormedToken(), event.card.id),
                        onFail = { _, _ ->
                            showMessageDialog(
                                "카드 삭제에 실패하였어요.",
                                "다시 한 번 시도해 주세요.",
                                showWarningImageView = true,
                                showCancelButton = true
                            ) { onDoneButtonClick { dismiss() } }
                        }
                    ) {
                        dismiss()
                        view?.showMessageBar("카드가 정상적으로 삭제되었어요.")
                        val inactiveDrawable = ContextCompat.getDrawable(
                            root.context,
                            R.drawable.bg_indicator_inactive
                        )
                        val adapter = cardPaymentInclude.cardList.adapter as? CardListAdapter
                        if (adapter != null) with(adapter) {
                            remove(event.card)
                            cardPaymentInclude.indicatorContainer.removeAllViews()
                            for (i in 0 until itemCount) {
                                cardPaymentInclude.indicatorContainer.addView(
                                    createDotView(
                                        inactiveDrawable
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}