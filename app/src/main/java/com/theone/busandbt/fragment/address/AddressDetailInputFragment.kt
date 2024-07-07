package com.theone.busandbt.fragment.address

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SPUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.DeliveryAddressAPI
import com.theone.busandbt.databinding.FragmentAddressDetailInputBinding
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.dto.address.Location
import com.theone.busandbt.dto.address.request.AddDeliveryAddressRequest
import com.theone.busandbt.dto.address.request.SetDeliveryAddressRequest
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.model.AppViewModel
import org.koin.android.ext.android.inject

/**
 * 상세 주소 입력, 장소 를 등록하는 창
 * TODO - 현재 리스트에서 선택 주소를 변경할 경우 서버에 업데이트를 하지 않아 DB에서 선택주소 값이 2개가 될 우려가 있다.
 */
class AddressDetailInputFragment : DataBindingFragment<FragmentAddressDetailInputBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_address_detail_input
    override val actionBarTitle: String = "상세 주소 입력"
    private val args by navArgs<AddressDetailInputFragmentArgs>()
    private val addressApi: DeliveryAddressAPI by inject()
    private var mode: Mode = Mode.ADD

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.item != null) mode = Mode.EDIT
        initShowingAddress()

        with(binding) {
            val typeface = Typeface.createFromAsset(context?.assets, "font/sult_medium.ttf")
            when (args.addressName) {
                "집" -> {
                    btnSelector(
                        addressSortInclude.homeTextView,
                        addressSortInclude.newLocationTextView,
                        addressSortInclude.companyTextView
                    )
                    imageChange(
                        addressSortInclude.homeLabel,
                        addressSortInclude.companyLabel,
                        addressSortInclude.newLocationLabel,
                        R.drawable.ic_orange_home,
                        R.drawable.ic_company,
                        R.drawable.ic_black_ping
                    )
                }
                "회사" -> {
                    btnSelector(
                        addressSortInclude.companyTextView,
                        addressSortInclude.homeTextView,
                        addressSortInclude.newLocationTextView
                    )
                    imageChange(
                        addressSortInclude.companyLabel,
                        addressSortInclude.homeLabel,
                        addressSortInclude.newLocationLabel,
                        R.drawable.ic_orange_company,
                        R.drawable.ic_home,
                        R.drawable.ic_black_ping
                    )
                }
                else -> {
                    btnSelector(
                        addressSortInclude.newLocationTextView,
                        addressSortInclude.homeTextView,
                        addressSortInclude.companyTextView
                    )
                    imageChange(
                        addressSortInclude.newLocationLabel,
                        addressSortInclude.homeLabel,
                        addressSortInclude.companyLabel,
                        R.drawable.ic_orange_location,
                        R.drawable.ic_home,
                        R.drawable.ic_company
                    )
                    newPlaceInclude.root.isVisible = true
                    newPlaceInclude.placeNameEditText.setText(args.addressName)
                }
            }

            //주소 입력하기
            addOrSetButton.setOnClickListener {
                //30개가 넘었을 땐 마지막 아이템 삭제 여부 아닐 땐 로직 진행
                val deliveryAddressList = deliveryAddressViewModel.getAllList()
                if (deliveryAddressList.size >= 30 && mode == Mode.ADD) {
                    showMessageDialog(
                        "주소 설정 가능한 개수를 초과했습니다.",
                        "사용하지 않는 주소는 삭제해 주세요.\n(최대 30개까지 저장가능)",
                        showWarningImageView = true
                    ) {
                        onDoneButtonClick { dismiss() }
                    }
                    return@setOnClickListener
                }
                val addressDetail = addressDetailEditText.text.toString()
                if (addressDetail.isEmpty()) {
                    it.showMessageBar("상세주소를 입력해주세요.")
                    return@setOnClickListener
                }
                val existAddress = args.item
                val searchedAddress = args.address
                val name = getAddressName() ?: return@setOnClickListener
                if (addressSortInclude.newLocationTextView.isSelected && name in arrayOf(
                        "집",
                        "회사"
                    )
                ) {
                    showMessageDialog(
                        "해당 별칭은 등록이 불가능합니다.",
                        "[집] 또는 [회사]를 선택하여 등록해 주세요",
                        showWarningImageView = true
                    ) {
                        onDoneButtonClick { dismiss() }
                    }
                    return@setOnClickListener
                }
                when (mode) {
                    Mode.ADD -> {
                        if (searchedAddress == null) return@setOnClickListener
                        val jibun = searchedAddress.jibun
                        val road = searchedAddress.road
                        val lat = searchedAddress.lat.toDouble()
                        val lng = searchedAddress.lng.toDouble()
                        addAddressRetrofit(
                            name,
                            jibun,
                            road,
                            addressDetail,
                            lat = lat,
                            lng = lng
                        )
                    }
                    Mode.EDIT -> {
                        if (existAddress == null) return@setOnClickListener
                        existAddress.addressDetail = addressDetail
                        setAddressRetrofit(
                            name,
                            existAddress
                        )
                    }
                }
            }
            addressLocation.setOnClickListener {
                val jibun = args.address?.jibun ?: args.item?.jibun ?: return@setOnClickListener
                val road = args.address?.road ?: args.item?.road ?: return@setOnClickListener
                val lat = args.address?.lat?.toDoubleOrNull() ?: args.item?.lat
                ?: return@setOnClickListener
                val lng = args.address?.lng?.toDoubleOrNull() ?: args.item?.lng
                ?: return@setOnClickListener
                val action =
                    AddressDetailInputFragmentDirections.actionAddressDetailFragmentToAddressLocationFragment(
                        Location(jibun, road, lat, lng)
                    )
                findNavController().navigate(action)
            }

            addressDetailEditText.setOnFocusChangeListener { _, _ ->
                addressDetailEditText.typeface = typeface
                addressDetailEditText.setBackgroundResource(R.drawable.bg_address_edittext_selector)
            }

            newPlaceInclude.placeNameEditText.setOnFocusChangeListener { _, _ ->
                newPlaceInclude.placeNameEditText.setBackgroundResource(R.drawable.bg_address_edittext_selector)
                newPlaceInclude.placeNameEditText.typeface = typeface
            }
            //클릭시 이미지색 변경, 버튼들 백그라운드 변경, 주소검색창 visible,gone
            addressSortInclude.homeTextView.setOnClickListener {
                when {
                    addressSortInclude.homeTextView.isSelected -> {
                        btnReSelector(
                            addressSortInclude.homeTextView,
                            addressSortInclude.homeLabel,
                            R.drawable.ic_home
                        )
                    }
                    else -> {
                        btnSelector(
                            addressSortInclude.homeTextView,
                            addressSortInclude.companyTextView,
                            addressSortInclude.newLocationTextView
                        )
                        imageChange(
                            addressSortInclude.homeLabel,
                            addressSortInclude.companyLabel,
                            addressSortInclude.newLocationLabel,
                            R.drawable.ic_orange_home,
                            R.drawable.ic_company,
                            R.drawable.ic_black_ping
                        )
                        newPlaceInclude.root.visibility = View.GONE
                    }
                }
            }
            addressSortInclude.companyTextView.setOnClickListener {
                when {
                    addressSortInclude.companyTextView.isSelected -> {
                        btnReSelector(
                            addressSortInclude.companyTextView,
                            addressSortInclude.companyLabel,
                            R.drawable.ic_company
                        )
                    }
                    else -> {
                        btnSelector(
                            addressSortInclude.companyTextView,
                            addressSortInclude.homeTextView,
                            addressSortInclude.newLocationTextView
                        )
                        imageChange(
                            addressSortInclude.companyLabel,
                            addressSortInclude.homeLabel,
                            addressSortInclude.newLocationLabel,
                            R.drawable.ic_orange_company,
                            R.drawable.ic_home,
                            R.drawable.ic_black_ping
                        )
                        newPlaceInclude.root.visibility = View.GONE
                    }
                }
            }
            addressSortInclude.newLocationTextView.setOnClickListener {
                when {
                    addressSortInclude.newLocationTextView.isSelected -> {
                        btnReSelector(
                            addressSortInclude.newLocationTextView,
                            addressSortInclude.newLocationLabel,
                            R.drawable.ic_black_ping
                        )
                        newPlaceInclude.root.visibility = View.GONE
                    }
                    else -> {
                        btnSelector(
                            addressSortInclude.newLocationTextView,
                            addressSortInclude.homeTextView,
                            addressSortInclude.companyTextView
                        )
                        imageChange(
                            addressSortInclude.newLocationLabel,
                            addressSortInclude.homeLabel,
                            addressSortInclude.companyLabel,
                            R.drawable.ic_orange_location,
                            R.drawable.ic_home,
                            R.drawable.ic_company
                        )
                        newPlaceInclude.root.visibility = View.VISIBLE
                    }
                }
            }
            addressDetailEditText.requestFocus()
        }

    }

    private fun getAddressName(): String? = with(binding) {
        when {
            addressSortInclude.homeTextView.isSelected -> "집"
            addressSortInclude.companyTextView.isSelected -> "회사"
            addressSortInclude.newLocationTextView.isSelected -> newPlaceInclude.placeNameEditText.text.toString()
                .takeIf { it.isNotEmpty() }
                ?: "새 장소"
            else -> null
        }
    }

    // 주소 등록  레트로핏 통신
    private fun addAddressRetrofit(
        name: String,
        jibun: String,
        road: String,
        addressDetail: String,
        lat: Double,
        lng: Double
    ) {
        val innerLoginInfo = loginInfo
        val old = deliveryAddressViewModel.selectedDeliveryAddress
        val added = addLocalDeliveryAddress(
            memberId = innerLoginInfo?.id ?: 0,
            name = name,
            jibun = jibun,
            road = road,
            addressDetail = addressDetail,
            lat = lat,
            lng = lng
        )
        completeAddDeliveryAddress()
        if (innerLoginInfo == null) return // 비로그인일 경우 종료
        if (old != null) {
            safeApiRequest(
                addressApi.setDeliveryAddress(
                    innerLoginInfo.getFormedToken(),
                    old.deliveryAddressId,
                    SetDeliveryAddressRequest(
                        enabled = false,
                        jibun = old.jibun,
                        road = old.road,
                        addressDetail = old.addressDetail,
                        lat = old.lat,
                        lng = old.lng
                    )
                )
            )
        }
        if (name in arrayOf("집", "회사") && old?.name == name) {
            return
        }
        safeApiRequest(
            addressApi.addDeliveryAddress(
                token = innerLoginInfo.getFormedToken(),
                AddDeliveryAddressRequest(
                    innerLoginInfo.id,
                    name,
                    jibun,
                    road,
                    addressDetail,
                    lat,
                    lng
                )
            )
        ) { response ->
            added.deliveryAddressId = response.deliveryAddressId
        }
    }

    private fun completeAddDeliveryAddress() {
        view?.showMessageBar("주소 등록이 완료되었습니다.")
        when (appViewModel.appState) {
            AppViewModel.AppState.FIRST_JOIN -> {
                // 최초 주소 등록 시 앱 초기 접속 완료값 저장하기
                // TODO - 주소 등록했다가 삭제하고 다시 등록할 경우는?
                val pref = SPUtils.getInstance("app")
                if (!pref.contains("init")) {
                    pref.put("init", true)
                    pref.put("firstAddAddress", true)
                }
                appViewModel.appState = AppViewModel.AppState.NORMAL
                findNavController().navigate(R.id.action_addressDetailFragment_to_mainFragment)
            }
            else -> findNavController().popBackStack(R.id.deliveryAddressListFragment, true)
        }
    }

    private fun addLocalDeliveryAddress(
        memberId: Int = 0,
        name: String,
        jibun: String,
        road: String,
        addressDetail: String,
        lat: Double,
        lng: Double
    ): DeliveryAddress {
        val a = DeliveryAddress(
            0,
            0,
            memberId,
            name,
            jibun,
            road,
            addressDetail,
            lat = lat,
            lng = lng,
            true
        )
        deliveryAddressViewModel.add(a)
        return a
    }

    //주소 수정 레트로핏 통신
    private fun setAddressRetrofit(
        addressName: String,
        existAddress: DeliveryAddress
    ) {
        with(existAddress) {
            val innerLoginInfo = loginInfo
            if (innerLoginInfo == null) {
                afterAddressEditDone(addressName, existAddress)
                return@with
            }
            val deliveryAddressId = args.item?.deliveryAddressId ?: return
            safeApiRequest(
                addressApi.setDeliveryAddress(
                    token = innerLoginInfo.getFormedToken(),
                    deliveryAddressId,
                    SetDeliveryAddressRequest(addressName, jibun, road, addressDetail, lat, lng, enabled)
                )
            ) { 
                afterAddressEditDone(addressName, existAddress)
            }
        }
    }

    private fun afterAddressEditDone(
        addressName: String,
        deliveryAddress: DeliveryAddress
    ) {
        view?.showMessageBar("주소 수정이 완료되었습니다.")
        deliveryAddress.name = addressName
        deliveryAddressViewModel.set(deliveryAddress)
        findNavController().popBackStack()
    }

    //받아온 주소로 주소값 변경 해준다
    private fun initShowingAddress() {
        with(binding) {
            when {
                args.address != null -> {
                    roadTextView.text = args.address?.road
                    buildingNameTextView.text = args.address?.name
                    jibunTextView.text = "(지번) " + args.address?.jibun
                }
                args.item != null -> {
                    roadTextView.text = args.item?.road
                    buildingNameTextView.isVisible = false
                    addressDetailEditText.setText(args.item?.addressDetail)
                    jibunTextView.text = args.item?.jibun
                }
            }
        }
    }


    //이미지 교체 함수
    private fun imageChange(
        orangeImg: AppCompatTextView,
        normalImg: AppCompatTextView,
        secnodNormalImg: AppCompatTextView,
        @DrawableRes orangeImgSource: Int,
        @DrawableRes normalSource: Int,
        @DrawableRes secondNormalSource: Int,
    ) {
        orangeImg.setCompoundDrawablesWithIntrinsicBounds(orangeImgSource, 0, 0, 0)
        normalImg.setCompoundDrawablesWithIntrinsicBounds(normalSource, 0, 0, 0)
        secnodNormalImg.setCompoundDrawablesWithIntrinsicBounds(secondNormalSource, 0, 0, 0)
        orangeImg.setTextColor(ColorUtils.getColor(R.color.mainColor))
        normalImg.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
        secnodNormalImg.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
    }

    //버튼을 다시 클릭 했을 때 원상복구
    private fun btnReSelector(
        changeBtn: AppCompatTextView,
        changeImg: AppCompatTextView,
        @DrawableRes changeImageResource: Int,
    ) {
        changeBtn.isSelected = false
        changeBtn.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
        changeImg.setCompoundDrawablesWithIntrinsicBounds(changeImageResource, 0, 0, 0)
        changeImg.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
    }

    //버튼 클릭했을 때 다른버튼들 기본 , 클릭된 버튼만 변경
    private fun btnSelector(
        firstBtn: AppCompatTextView,
        secondBtn: AppCompatTextView,
        thirdBtn: AppCompatTextView,
    ) {
        firstBtn.isSelected = true
        secondBtn.isSelected = false
        thirdBtn.isSelected = false
    }

    enum class Mode {
        ADD,
        EDIT
        ;
    }
}