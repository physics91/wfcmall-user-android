package com.theone.busandbt.fragment.address

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentShopLocationBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

/**
 * 매장의 상세위치를 나타내는 프래그먼트
 */
class ShopLocationFragment : DataBindingFragment<FragmentShopLocationBinding>(), OnMapReadyCallback,
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_shop_location
    override val actionBarTitle: String = "매장 위치"
    private val args by navArgs<ShopLocationFragmentArgs>()
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            location = args.location
            locationSource =
                FusedLocationSource(this@ShopLocationFragment, LOCATION_PERMISSION_REQUEST_CODE)
            mapView = binding.mapImg
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@ShopLocationFragment)

            if (args.enableMyLocation) {
                addressImg.isVisible = true
                //현재 권한여부를 묻는 기능이 회원가입에서 구현되지 않음. 임시로 권한 여부를 확인하도록 한다.
                val permissions: Array<String> = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                requestPermissions(
                    permissions,
                    0
                )
            }
        }
    }

    //권한여부를 확인하는 함수
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
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

    override fun onMapReady(mapImg: NaverMap) {
        Log.d(TAG, "onMapReady")
        //좌표 값 부여
        val coord = LatLng(args.location.lat, args.location.lng)
        var firstClick = true
        val locationOverlay = mapImg.locationOverlay
        var cameraUpdate = CameraUpdate.scrollTo(coord)
        val marker = Marker()

        //위치 소스 지정
        mapImg.locationSource = locationSource

        //지도 구현시 초기 화면 위치 및 표시 범위 설정
        mapImg.moveCamera(cameraUpdate)
        mapImg.minZoom = 7.0
        mapImg.maxZoom = 19.0

        //지도상에 마커 표시
        marker.position = coord
        marker.map = mapImg
        marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
        marker.width = SizeUtils.dp2px(40f)
        marker.height = SizeUtils.dp2px(60f)

        //UI 컨트롤 재배치
        val uiSettings = mapImg.uiSettings
        uiSettings.isLogoClickEnabled = false
        uiSettings.isCompassEnabled = false
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = false

        //접근 권한 확인 후 위치 추적 모드 활성화.
        binding.addressImg.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //위치 오버레이의 초기 위치를 매장으로 변경. 이후 클릭이벤트에서 동작하지 않는다.
                if (firstClick) {
                    locationOverlay.isVisible = true
                    locationOverlay.position = coord
                    firstClick = false
                }
                //내 위치 버튼 클릭시 위치 추적 활성화. 한번 더 클릭하면 현재 방향을 확인 할 수 있다.
                binding.addressImg.isSelected = !binding.addressImg.isSelected
                when (binding.addressImg.isSelected) {
                    true -> {
                        cameraUpdate = CameraUpdate.zoomTo(15.0)
                        mapImg.moveCamera(cameraUpdate)
                        mapImg.locationTrackingMode = LocationTrackingMode.Follow
                    }
                    false -> mapImg.locationTrackingMode = LocationTrackingMode.NoFollow
                }
                //접근 권한이 없을 시 이벤트 종료
            } else {
                return@setOnClickListener
            }
        }
    }

    //프래그먼트 재시작시 mapView 호출
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    companion object {
        private const val TAG = "FullAddressFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}