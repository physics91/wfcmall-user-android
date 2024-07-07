package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentShopDetailsPackagingBinding
import com.theone.busandbt.dto.address.Location
import com.theone.busandbt.dto.shop.ShopDetail
import com.theone.busandbt.extension.getParcelableCompat
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.naver.maps.geometry.Coord
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

/**
 * 방문포장 화면
 */
class ShopDetailPackagingFragment : DataBindingFragment<FragmentShopDetailsPackagingBinding>(),
    OnMapReadyCallback {
    override val layoutId: Int = R.layout.fragment_shop_details_packaging
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var coord: Coord
    private val mapView: MapView get() = binding.map

    /**
     * TODO - 포장할인 디자인이 지금 이미지로 박혀있음 -> 유동적이게 변경해야함
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val sd = args.getParcelableCompat<ShopDetail>("shopDetail") ?: return
        with(binding) {
            shopDetail = sd
            paymentText.text = sd.paymentTypeNameList.joinToString(",")
            if (sd.packagingDiscountCost > 0) {
                val costText =
                    getString(R.string.commonCost, sd.packagingDiscountCost.toMoneyFormat())
                packagingDiscountTextView.text = "포장 주문 시 $costText 할인!"
                packagingDiscountTextView.isVisible = true
            }
            mLocationSource = FusedLocationSource(
                this@ShopDetailPackagingFragment,
                LOCATION_PERMISSION_REQUEST_CODE
            )
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@ShopDetailPackagingFragment)
            fullAddress.setOnClickListener {
                val action = ShopDetailFragmentDirections.actionShopDetailsFragmentToFullAddress(
                    Location(
                        sd.jibun,
                        sd.road,
                        sd.lat,
                        sd.lng
                    ), true
                )
                findNavController().navigate(action)
            }
            addressText.text = buildSpannedString {
                val addressText =
                    "${sd.road.takeIf { it.isNotEmpty() } ?: sd.jibun} ${sd.addressDetail} "
                append(addressText)
                val addressCopyText = " 주소복사"
                append(
                    addressCopyText,
                    ForegroundColorSpan(MAIN_COLOR),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val index = addressText.length
                setSpan(
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(requireContext(), R.font.sult_bold) ?: return@with
                    ), index, index + addressCopyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            addressText.setOnClickListener {
                ClipboardUtils.copyText("${sd.road} ${sd.addressDetail}")
                it.showMessageBar("주소복사가 완료되었어요.")
            }
        }
    }

    //OnResume 상태일 때 requestLayout()을 통해 onMeasure가 호출 되므로 Size가 업데이트 된다.
    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    override fun onMapReady(mapImg: NaverMap) {
        val sd = binding.shopDetail ?: return
        coord = LatLng(sd.lat, sd.lng)
        val cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                (coord as LatLng).latitude + 0.000135,
                (coord as LatLng).longitude
            )
        )
        val marker = Marker()
        val uiSettings = mapImg.uiSettings
        //카메라 위치,줌 설정
        mapImg.moveCamera(cameraUpdate)
        mapImg.minZoom = 17.0
        mapImg.maxZoom = 17.0
        //지도상에 마커 표시
        marker.position = coord as LatLng
        marker.map = mapImg
        marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
        marker.width = SizeUtils.dp2px(40f)
        marker.height = SizeUtils.dp2px(60f)
        //UI 컨트롤 재배치
        uiSettings.isCompassEnabled = false
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = false
    }

    //프래그먼트 재시작시 mapView 호출
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}