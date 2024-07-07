package com.theone.busandbt.fragment.address

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.Coord
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.outside.KakaoAPI
import com.theone.busandbt.databinding.FragmentAddressFineByCurrentLocationBinding
import com.theone.busandbt.dto.address.AddressSearchResultItem
import com.theone.busandbt.extension.getJibunDetail
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.type.RegionType
import com.theone.busandbt.utils.UnitConverter
import org.koin.android.ext.android.inject
import java.util.Locale

/**
 * 현재 위치로 주소찾기 클릭 시 나오는 지도맵 입니다.
 */
class AddressFineFragment : DataBindingFragment<FragmentAddressFineByCurrentLocationBinding>(),
    OnMapReadyCallback, EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_address_fine_by_current_location
    override val actionBarTitle: String = "지도에서 주소 찾기"
    private lateinit var locationSource: FusedLocationSource
    private lateinit var coord: Coord
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationMarker: Marker
    private lateinit var sortText: List<String>
    private val kakaoAPI: KakaoAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            locationSource =
                FusedLocationSource(this@AddressFineFragment, LOCATION_PERMISSION_REQUEST_CODE)
            mapView = mapImg
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@AddressFineFragment)

            //현재 권한여부를 묻는 기능이 회원가입에서 구현되지 않음. 임시로 권한 여부를 확인하도록 한다.
            val permissions: Array<String> = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissions(
                permissions,
                1
            )
            settingBtn.setOnClickListener {
                sortText = jibunTextView.text.split("\n")
                val center = naverMap.cameraPosition.target
                val action =
                    AddressFineFragmentDirections.actionAddressFineFragmentToAddressDetailFragment(
                        address = AddressSearchResultItem(
                            name = buildingNameTextView.text.toString(),
                            jibun = jibunTextView.text.toString(),
                            road = roadTextView.text.toString(),
                            lat = center.latitude.toString(),
                            lng = center.longitude.toString()
                        ),
                        addressName = null
                    )
                findNavController().navigate(action)
            }
            exit.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    //권한여부를 확인하는 함수
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val a = activity ?: return
        val client = LocationServices.getFusedLocationProviderClient(a)
        if (ActivityCompat.checkSelfPermission(
                a,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                a,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        client.lastLocation.addOnCompleteListener(a) { task ->
            val location: Location? = task.result
            if (location != null) {
                val geocoder = Geocoder(a, Locale.getDefault())
                val list =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        ?: return@addOnCompleteListener
                val loc = list.firstOrNull()
                coord =
                    if (loc != null) LatLng(loc.latitude, loc.longitude) else LatLng(
                        35.173550,
                        129.124781
                    )
                val cameraUpdate = CameraUpdate.scrollTo(coord as LatLng)
                val locationOverlay = naverMap.locationOverlay
                naverMap.moveCamera(cameraUpdate)

                locationMarker = Marker()

                //위치 소스 지정
                naverMap.locationSource = locationSource

                //지도 구현시 초기 화면 위치 및 줌범위 설정
                naverMap.moveCamera(cameraUpdate)
                naverMap.minZoom = 7.0
                naverMap.maxZoom = 19.0
                naverMap.addOnCameraIdleListener {
                    loadAddress()
                }

                //지도상에 마커 표시
                locationMarker.position = coord as LatLng
                locationMarker.map = naverMap
                locationMarker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
                locationMarker.width = UnitConverter.dpToPx(40)
                locationMarker.height = UnitConverter.dpToPx(60)

                this.naverMap.addOnCameraChangeListener { i, b ->
                    val center = this.naverMap.cameraPosition.target
                    locationMarker.position = LatLng(center.latitude, center.longitude)
                }

                //UI 컨트롤 재설정
                val uiSettings = naverMap.uiSettings
                uiSettings.isLogoClickEnabled = false
                uiSettings.isCompassEnabled = false
                uiSettings.isScaleBarEnabled = false
                uiSettings.isZoomControlEnabled = false
                uiSettings.isLocationButtonEnabled = false

                loadAddress()
            }
        }
        this.naverMap = naverMap
        // 좌표 값 부여 현재 위치 체크해서 할 것
//        coord = LatLng(35.173550, 129.124781)

        //접근 권한 확인 후 위치 추적 모드 활성화.
//        binding.addressImg.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(
//                    requireActivity(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(
//                    requireActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                //위치 오버레이의 초기 위치를 매장으로 변경. 이후 클릭이벤트에서 동작하지 않는다.
//                if (firstClick) {
//                    locationOverlay.isVisible = true
//                    locationOverlay.position = coord as LatLng
//                    firstClick = false
//                }
//                //내 위치 버튼 클릭시 위치 추적 활성화. 한번 더 클릭하면 현재 방향을 확인 할 수 있다.
//                binding.addressImg.isSelected = !binding.addressImg.isSelected
//                when (binding.addressImg.isSelected) {
//                    true -> {
//                        cameraUpdate = CameraUpdate.zoomTo(15.0)
//                        naverMap.moveCamera(cameraUpdate)
//                        naverMap.locationTrackingMode = LocationTrackingMode.Follow
//                    }
//                    false -> naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
//                }
//                //접근 권한이 없을 시 이벤트 종료
//            } else {
//                return@setOnClickListener
//            }
//        }
    }

    private fun loadAddress() {
        val center = naverMap.cameraPosition.target
        val lng = center.longitude.toString()
        val lat = center.latitude.toString()
        safeApiRequest(
            kakaoAPI.getSearchLocalAddress(lng, lat),
            showProgress = false
        ) {
            val address = it.documents.firstOrNull() ?: return@safeApiRequest
            with(binding) {
                roadTextView.text = address.road?.name ?: "도로명주소가 없습니다."
                val buildingName = address.road?.buildingName ?: ""
                buildingNameTextView.text = buildingName
                buildingNameTextView.isVisible = buildingName.isNotEmpty()
                safeApiRequest(
                    kakaoAPI.getSearchRegion(lng, lat),
                    showProgress = false
                ) { response ->
                    val admin =
                        response.documents.find { document -> document.regionType == RegionType.ADMIN.code }
                    jibunTextView.text =
                        if (admin != null) "${admin.addressName} ${address.jibun?.name?.getJibunDetail()}".trimEnd() else address.jibun?.name
                            ?: ""
                }
            }
        }
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