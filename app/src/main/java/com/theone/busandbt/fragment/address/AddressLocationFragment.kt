package com.theone.busandbt.fragment.address

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.outside.KakaoAPI
import com.theone.busandbt.databinding.FragmentAddressLocationBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.theone.busandbt.extension.safeApiRequest
import org.koin.android.ext.android.inject


/**
 * 지도에서 위치보기 눌렸을 때 나오는 지도맵 부분입니다.
 */
class AddressLocationFragment : DataBindingFragment<FragmentAddressLocationBinding>(),
    OnMapReadyCallback, EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_address_location
    override val actionBarTitle: String = "지도에서 위치보기"
    private val args by navArgs<AddressLocationFragmentArgs>()
    private lateinit var mapView: MapView
    private val kakaoAPI: KakaoAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            location = args.location
            mapView = mapImg
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@AddressLocationFragment)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        val coord = LatLng(args.location.lat, args.location.lng)
        val cameraUpdate = CameraUpdate.scrollTo(coord)
        val marker = Marker()

        //지도 구현시 초기 화면 위치 및 줌범위 설정
        naverMap.moveCamera(cameraUpdate)
        val coordRange =
            LatLng(args.location.lat + 0.0000001, args.location.lng + 0.0000001)
        naverMap.extent = LatLngBounds(coord, coordRange)
        naverMap.minZoom = 7.0
        naverMap.maxZoom = 19.0
        naverMap.addOnCameraIdleListener {
            val center = naverMap.cameraPosition.target
            safeApiRequest(
                kakaoAPI.getSearchLocalAddress(center.longitude.toString(), center.latitude.toString())
            ) {
                val address = it.documents.firstOrNull() ?: return@safeApiRequest
                with(binding) {
                    roadTextView.text = address.road?.name ?: "도로명주소가 없습니다."
                    val buildingName = address.road?.buildingName ?: ""
                    buildingNameTextView.text = buildingName
                    buildingNameTextView.isVisible = buildingName.isNotEmpty()
                    jibunTextView.text = address.jibun?.name ?: ""
                }
            }
        }

        //지도상에 마커 표시
        marker.position = coord
        marker.map = naverMap
        marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
        marker.width = SizeUtils.dp2px(40f)
        marker.height = SizeUtils.dp2px(60f)

        //UI 컨트롤 재배치
        val uiSettings = naverMap.uiSettings
        uiSettings.isLogoClickEnabled = false
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
}