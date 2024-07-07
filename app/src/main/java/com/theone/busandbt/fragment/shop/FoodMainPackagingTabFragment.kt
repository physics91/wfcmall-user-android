package com.theone.busandbt.fragment.shop

import android.Manifest
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.busandbt.code.ShopSortType
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.FoodListAdapter
import com.theone.busandbt.adapter.shop.PackagingMapShopListAdapter
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentFoodMainPackagingTabBinding
import com.theone.busandbt.dialog.CategorySelectionDialog
import com.theone.busandbt.dialog.DistanceSelectionDialog
import com.theone.busandbt.dialog.selection.ShopSortTypeSelectionDialog
import com.theone.busandbt.dto.Selection
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.dto.shop.ShopListItem
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.getParcelableCompat
import com.theone.busandbt.extension.hasLocationPermission
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.setOnReceiveData
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.instance.MAIN_TEXT_COLOR
import com.theone.busandbt.model.main.ServiceMainViewModel
import com.theone.busandbt.utils.COMMON_DATA_COUNT
import com.theone.busandbt.utils.UnitConverter
import org.koin.android.ext.android.inject
import retrofit2.Call
import kotlin.collections.set

/**
 * 음식점 메인 포장주문 프래그먼트
 */
@Suppress("UNCHECKED_CAST")
class FoodMainPackagingTabFragment :
    SingleListFragment<FragmentFoodMainPackagingTabBinding, FoodListAdapter, ShopListItem>(),
    OnMapReadyCallback {

    override val layoutId: Int = R.layout.fragment_food_main_packaging_tab
    override val recyclerView: RecyclerView get() = binding.restaurantList
    override lateinit var customScrollView: NestedScrollView
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var infoWindow: InfoWindow
    private var categorySelectionDialog: CategorySelectionDialog? = null
    private var categorySelectionDialogForMap: CategorySelectionDialog? = null
    private var shopSortTypeSelectionDialog: ShopSortTypeSelectionDialog? = null
    private var distanceSelectionDialog: DistanceSelectionDialog? = null
    private var selectedCategory: CategoryListItem? = null
    private var selectedCategoryForMap: CategoryListItem? = null
    private var selectedDistance: Double? = null
    private var scrollLock = true
    private var selectedShopSortType: ShopSortType? = null
    private var showInfoMarker: Marker? = null
    private val shopAPI: ShopAPI by inject()
    private val parentViewModel: ServiceMainViewModel by viewModels({ requireParentFragment() })
    private val shopMarkerMap = HashMap<LatLng, Marker>()

    override fun makeAdapter(): FoodListAdapter = FoodListAdapter(DeliveryType.PACKAGING)

    override fun getCall(): Call<List<ShopListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: error("잘못된 접근")
        return shopAPI.getShopList(
            page,
            COMMON_DATA_COUNT,
            jibun = da.jibun,
            shopSortType = selectedShopSortType?.id ?: ShopSortType.NEAR.id,
            distance = selectedDistance,
            customerLat = da.lat,
            customerLng = da.lng,
            coupon = if (binding.couponBadge.isSelected) true else null,
            packaging = true,
            serviceType = ServiceType.FOOD_DELIVERY.id,
            deliveryType = DeliveryType.PACKAGING.id,
            categoryId = selectedCategory?.id
        )
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        appViewModel.basketServiceType = ServiceType.FOOD_DELIVERY
        appViewModel.basketDeliveryType = DeliveryType.PACKAGING
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        customScrollView = binding.contentScrollView // 먼저 초기화가 돼야한다.
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            contentScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                if (!mapViewGroup.isVisible) parentViewModel.setScrollY(scrollY)
            })
            locationSource = FusedLocationSource(
                this@FoodMainPackagingTabFragment,
                LOCATION_PERMISSION_REQUEST_CODE
            )
            mapView = mapImg
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@FoodMainPackagingTabFragment)

            //현재 권한여부를 묻는 기능이 회원가입에서 구현되지 않음. 임시로 권한 여부를 확인하도록 한다.
            val permissions: Array<String> = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissions(permissions, 0)
            recyclerViewAdapter.setOnItemClick { _, _, item ->
                val action =
                    ServiceMainFragmentDirections.actionRestaurantMainFragmentToShopDetailsFragment(
                        item.id,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.PACKAGING.id
                    )
                findNavController().navigate(action)
            }

            deliveryAddressViewModel.observeSelectedAddress(viewLifecycleOwner) {
                if (it == null) return@observeSelectedAddress
                refresh()
            }

            initClickListener()
            initShopListAndMapToggler()
            shopListRecyclerView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shopSortTypeSelectionDialog?.recycle(this)
        categorySelectionDialog?.recycle(this)
        categorySelectionDialogForMap?.recycle(this)
        childFragmentManager.clearFragmentResult(DISTANCE_REQUEST_KEY)
    }

    private fun initClickListener() {
        with(binding) {
            categorySelectionForm.setOnClickListener {
                initCategorySelectionDialog(ViewMode.LIST)
                showCategorySelectionDialog(ViewMode.LIST)
            }
            categorySelectionFormForMap.setOnClickListener {
                initCategorySelectionDialog(ViewMode.MAP)
                showCategorySelectionDialog(ViewMode.MAP)
            }
            distanceSelectionForm.setOnClickListener {
                if (distanceSelectionDialog == null) initDistanceSelectionDialog()
                val dsd = distanceSelectionDialog ?: return@setOnClickListener
                dsd.show(childFragmentManager, null)
            }
            sort.setOnClickListener {
                initShopSortSelectionDialog()
                showShopSortSelectionDialog()
            }
            returnImg.setOnClickListener {
                returnImg()
                selectedCategory = null
                selectedShopSortType = null
                selectedDistance = null
                updateFilterInitButton(ViewMode.LIST)
                distanceSelectionDialog?.init()
                refresh()
            }
            returnImgForMap.setOnClickListener {
                returnImgForMap()
                selectedCategoryForMap = null
                updateFilterInitButton(ViewMode.MAP)
                refreshMap(naverMap)
            }

            upBtn.setOnClickListener {
                restaurantList.smoothScrollToPosition(0)
            }
            initCouponBadge()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(map: NaverMap) {
        with(binding) {
            val da = deliveryAddressViewModel.selectedDeliveryAddress
            val coord = LatLng(da?.lat ?: 0.0, da?.lng ?: 0.0)
            var firstClick = true
            val locationOverlay = map.locationOverlay
            var cameraUpdate = CameraUpdate.scrollTo(coord)
            val myMarker = Marker()
            infoWindow = InfoWindow()
            naverMap = map

            // TODO - 움직이고 있을때 다시 요청할 경우 마지막 지도 움직임때만 다시 요청하도록 변경되어야한다.
            naverMap.addOnCameraIdleListener {
                refreshMap(map)
            }

            //위치 소스 지정
            map.locationSource = locationSource
            //지도 구현시 초기 화면 위치 및 줌범위 설정
            map.moveCamera(cameraUpdate)
            map.moveCamera(CameraUpdate.zoomTo(16.0))
            map.minZoom = 7.0
            map.maxZoom = 19.0

            //네이버 맵 클릭시 정보창과 메뉴 리사이클러뷰 닫기
            map.setOnMapClickListener { _, _ ->
                if (shopListRecyclerView.isVisible || infoWindow.isVisible) {
                    shopListRecyclerView.isVisible = false
                    infoWindow.close()
                    infoWindow.position = LatLng(0.0, 0.0)
                    showInfoMarker?.map = map
                    showInfoMarker = null
                }
            }

            //지도상에 마커 표시
            myMarker.position = coord
            myMarker.map = map
            myMarker.icon = OverlayImage.fromResource(R.drawable.ic_map_my_marker)
            myMarker.width = UnitConverter.dpToPx(44)
            myMarker.height = UnitConverter.dpToPx(62)

            //UI 컨트롤 재설정
            val uiSettings = map.uiSettings
            uiSettings.isLogoClickEnabled = false
            uiSettings.isCompassEnabled = false
            uiSettings.isScaleBarEnabled = false
            uiSettings.isZoomControlEnabled = false
            uiSettings.isLocationButtonEnabled = false

            //접근 권한 확인 후 위치 추적 모드 활성화.
            goMyLocationButton.setOnClickListener {
                if (!requireActivity().hasLocationPermission()) return@setOnClickListener
                //위치 오버레이의 초기 위치를 매장으로 변경. 이후 클릭이벤트에서 동작하지 않는다.
                if (firstClick) {
                    locationOverlay.isVisible = true
                    locationOverlay.position = coord
                    firstClick = false
                }
                //내 위치 버튼 클릭시 위치 추적 활성화. 한번 더 클릭하면 현재 방향을 확인 할 수 있다.
                goMyLocationButton.isSelected = !goMyLocationButton.isSelected
                if (goMyLocationButton.isSelected) {
                    cameraUpdate = CameraUpdate.zoomTo(15.0)
                    map.moveCamera(cameraUpdate)
                    map.locationTrackingMode = LocationTrackingMode.Follow
                } else {
                    map.locationTrackingMode = LocationTrackingMode.NoFollow
                }
            }
        }
    }

    private fun refreshMap(map: NaverMap) {
        val innerDeliveryAddress = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        val center = naverMap.cameraPosition.target
        val projection = map.projection
        val factor = if (map.width > map.height) map.width else map.height
        safeApiRequest(
            shopAPI.getShopLocationList(
                centerLng = center.longitude,
                centerLat = center.latitude,
                jibun = innerDeliveryAddress.jibun,
                distance = projection.metersPerPixel * (factor / 2),
                serviceType = ServiceType.FOOD_DELIVERY.id,
                deliveryType = DeliveryType.PACKAGING.id,
                hasCoupon = if (binding.couponBadgeForMap.isSelected) true else null,
                categoryId = selectedCategoryForMap?.id
            ),
            showProgress = false
        ) { response ->
            shopMarkerMap.values.forEach { marker -> marker.map = null }
            shopMarkerMap.clear()
            val sim = showInfoMarker
            if (infoWindow.map != null && sim != null) {
                shopMarkerMap[infoWindow.position] = sim
            }
            response.forEach { location ->
                val position = LatLng(location.lat, location.lng)
                if (infoWindow.map != null && infoWindow.position == position) return@forEach
                if (shopMarkerMap.containsKey(position)) {
                    val marker = shopMarkerMap[position] ?: return@forEach
                    val tag = marker.tag as? ArrayList<Int> ?: return@forEach
                    tag.add(location.id)
                    marker.tag = tag
                    return@forEach
                }
                val marker = Marker()
                marker.position = position
                marker.map = naverMap
                marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
                marker.width = SizeUtils.dp2px(40f)
                marker.height = SizeUtils.dp2px(60f)
                marker.tag = arrayListOf(location.id)
                shopMarkerMap[position] = marker

                marker.setOnClickListener {
                    val params =
                        marker.tag as? List<Int> ?: return@setOnClickListener true
                    safeApiRequest(
                        shopAPI.getPackagingShopInfoList(params)
                    ) depth2@{
                        if (it.isEmpty()) return@depth2
                        (binding.shopListRecyclerView.adapter as? PackagingMapShopListAdapter)?.run {
                            setItems(it)
                        }
                        binding.shopListRecyclerView.isVisible = true
                        val infoText = buildString {
                            var shopName = it.first().name
                            if (shopName.length >= 7) shopName =
                                shopName.substring(0..5) + "…"
                            append(shopName)
                            if (it.size > 1) append(" 외 ${it.size - 1}건")
                        }
                        infoWindow.adapter = object : InfoWindow.ViewAdapter() {
                            override fun getView(p0: InfoWindow): View {
                                val view =
                                    View.inflate(
                                        requireContext(),
                                        R.layout.popup_map_shop_info,
                                        null
                                    )
                                (view.findViewById<View>(R.id.shopNameTextView) as TextView).text =
                                    infoText
                                return view
                            }
                        }
                        infoWindow.anchor = PointF(0.163f, 1.0f)
                        infoWindow.position = position
                        infoWindow.open(map)
                        showInfoMarker = marker
                        marker.map = null
                    }

                    return@setOnClickListener true
                }
            }
        }
    }

    private fun toggleCouponBadge(couponBadge: TextView) {
        if (!couponBadge.isSelected) {
            couponBadge.setTextColor(MAIN_COLOR)
            couponBadge.setBackgroundResource(R.drawable.bg_address_choice_text)
        } else {
            couponBadge.setTextColor(MAIN_TEXT_COLOR)
            couponBadge.setBackgroundResource(R.drawable.bg_filter_item)
        }
        couponBadge.isSelected = !couponBadge.isSelected
    }

    private fun initCouponBadge() {
        with(binding) {
            couponBadge.setOnClickListener {
                toggleCouponBadge(couponBadge)
                updateFilterInitButton(ViewMode.LIST)
                refresh()
            }
            couponBadgeForMap.setOnClickListener {
                toggleCouponBadge(couponBadgeForMap)
                updateFilterInitButton(ViewMode.MAP)
                refreshMap(naverMap)
            }
        }
    }

    // 모든 뷰들 원래대로 돌리기
    private fun returnView(img: TextView, title: String) {
        img.text = title
        img.setBackgroundResource(R.drawable.bg_filter_item)
        img.setTextColor(MAIN_TEXT_COLOR)
        img.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_down_arrow,
            0
        )
    }

    //초기화 버튼 이벤트
    private fun returnImg() {
        with(binding) {
            returnView(sort, "가까운 순")
            returnView(categorySelectionForm, "메뉴 전체")
            returnView(distanceSelectionForm, "거리")
            couponBadge.setBackgroundResource(R.drawable.bg_filter_item)
            couponBadge.setTextColor(MAIN_TEXT_COLOR)
            couponBadge.isSelected = false
        }
    }

    private fun returnImgForMap() {
        with(binding) {
            returnView(categorySelectionFormForMap, "메뉴 전체")
            couponBadgeForMap.setBackgroundResource(R.drawable.bg_filter_item)
            couponBadgeForMap.setTextColor(MAIN_TEXT_COLOR)
            couponBadgeForMap.isSelected = false
        }
    }

    //맵 내부 리사이클러뷰
    private fun shopListRecyclerView() {
        with(binding) {
            shopListRecyclerView.adapter = PackagingMapShopListAdapter().apply {
                setOnItemClick { _, _, item ->
                    val action =
                        ServiceMainFragmentDirections.actionRestaurantMainFragmentToShopDetailsFragment(
                            item.id,
                            ServiceType.FOOD_DELIVERY.id,
                            DeliveryType.PACKAGING.id
                        )
                    findNavController().navigate(action)
                }
            }
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(shopListRecyclerView)
        }
    }

    private fun showShopSortSelectionDialog() {
        shopSortTypeSelectionDialog?.run {
            setDefault(selectedShopSortType)
            show(this@FoodMainPackagingTabFragment.childFragmentManager, null)
        }
    }

    private fun initShopSortSelectionDialog() {
        if (shopSortTypeSelectionDialog != null) return
        shopSortTypeSelectionDialog = ShopSortTypeSelectionDialog(
            ShopSortType.values().filter { it.id > 0 }.map { Selection(it.desc(), it) }
        ).apply {
            setOnReceiveData(this@FoodMainPackagingTabFragment) {
                if (it == null) return@setOnReceiveData
                selectedShopSortType = it
                with(binding.sort) {
                    text = it.desc()
                    setBackgroundResource(R.drawable.bg_address_choice_text)
                    setTextColor(MAIN_COLOR)
                    setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_down_arrow_main_color,
                        0
                    )
                }
                updateFilterInitButton(ViewMode.LIST)
                refresh()
            }
        }
    }

    private fun showCategorySelectionDialog(mode: ViewMode) {
        val cf = childFragmentManager
        when (mode) {
            ViewMode.LIST -> categorySelectionDialog?.run {
                show(cf, tag)
            }

            ViewMode.MAP -> categorySelectionDialogForMap?.run {
                show(cf, tag)
            }
        }
    }

    private fun initCategorySelectionDialog(mode: ViewMode) {
        when (mode) {
            ViewMode.LIST -> {
                if (categorySelectionDialog != null) return
                categorySelectionDialog = CategorySelectionDialog(mode.key)
            }

            ViewMode.MAP -> {
                if (categorySelectionDialogForMap != null) return
                categorySelectionDialogForMap = CategorySelectionDialog(mode.key)
            }
        }
        childFragmentManager.setFragmentResultListener(
            mode.key,
            this@FoodMainPackagingTabFragment
        ) { _, bundle ->
            val categoryListItem = bundle.getParcelableCompat<CategoryListItem>("item")
                ?: return@setFragmentResultListener
            when (mode) {
                ViewMode.LIST -> selectedCategory = categoryListItem
                ViewMode.MAP -> selectedCategoryForMap = categoryListItem
            }
            val form = when (mode) {
                ViewMode.LIST -> binding.categorySelectionForm
                ViewMode.MAP -> binding.categorySelectionFormForMap
            }
            form.text = categoryListItem.name
            form.setBackgroundResource(R.drawable.bg_address_choice_text)
            form.setTextColor(MAIN_COLOR)
            form.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_down_arrow_main_color,
                0
            )
            updateFilterInitButton(mode)
            when (mode) {
                ViewMode.LIST -> refresh()
                ViewMode.MAP -> refreshMap(naverMap)
            }
        }
    }

    private fun initDistanceSelectionDialog() {
        with(binding) {
            distanceSelectionDialog = DistanceSelectionDialog()
            childFragmentManager.setFragmentResultListener(
                DISTANCE_REQUEST_KEY,
                viewLifecycleOwner
            ) { _, bundle ->
                val newDistance = bundle.getDouble(DISTANCE_REQUEST_KEY)
                if (selectedDistance == newDistance) return@setFragmentResultListener
                selectedDistance = newDistance
                distanceSelectionForm.text = "${newDistance}km 이내"
                distanceSelectionForm.setBackgroundResource(R.drawable.bg_address_choice_text)
                distanceSelectionForm.setTextColor(MAIN_COLOR)
                distanceSelectionForm.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow_main_color,
                    0
                )
                updateFilterInitButton(ViewMode.LIST)
                refresh()
            }
        }
    }

    //지도 뷰, 리사이클러뷰 클릭 시 visibilty,Gone
    private fun initShopListAndMapToggler() {
        with(binding) {
            changeImg.setOnClickListener {
                scrollLock = !scrollLock
                setFragmentResult("scrollLock", bundleOf("lock" to scrollLock))
                changeImg.isSelected = !changeImg.isSelected
                when {
                    mapViewGroup.visibility == View.VISIBLE -> {
                        parentViewModel.setBannerFormVisible(true)
                        recyclerView.visibility = View.VISIBLE
                        mapViewGroup.visibility = View.GONE
                        goMyLocationButton.visibility = View.GONE
                        addressBack.visibility = View.GONE
                        sort.visibility = View.VISIBLE
                        shopScrollableHost.visibility = View.GONE
                    }

                    recyclerView.visibility == View.VISIBLE -> {
                        parentViewModel.setBannerFormVisible(false)
                        mapViewGroup.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        goMyLocationButton.visibility = View.VISIBLE
                        addressBack.visibility = View.VISIBLE
                        sort.visibility = View.GONE
                        shopScrollableHost.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /**
     * 현재 필터 상태에 따라 초기화 버튼의 상태를 지정한다.
     */
    private fun updateFilterInitButton(mode: ViewMode) {
        with(binding) {
            when (mode) {
                ViewMode.LIST -> {
                    val flag =
                        selectedCategory != null || selectedDistance != null || selectedShopSortType != null || couponBadge.isSelected
                    returnImg.isVisible = flag
                }

                ViewMode.MAP -> {
                    val flag =
                        selectedCategoryForMap != null || couponBadgeForMap.isSelected
                    returnImgForMap.isVisible = flag
                }
            }
        }
    }

    private enum class ViewMode(val key: String) {
        LIST("list"),
        MAP("map"),
        ;
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val DISTANCE_REQUEST_KEY = "distance"
    }
}
