//package com.busan.newdbt.service
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.content.Intent
//import android.location.Location
//import android.location.LocationManager
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.blankj.utilcode.util.ColorUtils
//import com.nhn.android.maps.NMapLocationManager
//import com.nhn.android.maps.maplib.NGeoPoint
//import com.please.android.pleaseandroidapp.R
//import com.please.android.pleaseandroidapp.dto.MLoc
//import com.please.android.pleaseandroidapp.eventbus.ChangedMyLocationEvent
//import com.please.android.pleaseandroidapp.extension.debugCode
//import com.please.android.pleaseandroidapp.util.AndroidLocationUtil
//import com.pleaze.android.pleazeandroidcommon.extension.debugTry
//import com.pleaze.android.pleazeandroidcommon.extension.runAsync
//import com.pleaze.android.pleazeandroidcommon.persistence.AppUserInfo
//import com.pleaze.android.pleazeandroidcommon.persistence.RestTree
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import org.greenrobot.eventbus.EventBus
//
///**
// * 위치를 수집하는 포그라운드 서비스입니다.
// * 현재는 기사의 위치만 수집하는 목적으로 만들어져 있습니다.
// * @author jaeyeong
// * @since unknown
// */
//class LocationService {
//
//    private var mLocationManager: NMapLocationManager? = null
//    private var mLocationListeners = arrayOf(
//        LocationListener(LocationManager.GPS_PROVIDER),
//        LocationListener(LocationManager.NETWORK_PROVIDER)
//    )
//    private var checkJob: Job? = null
//    private val locList = ArrayList<MLoc>()
////    private val locationListener = object : LocationListener {
////        override fun onLocationChanged(location: Location) {
////            val c = this@LocationService
////            if (location.accuracy <= 10.0 && location.provider == "gps") {
////                debugCode { Log.e("내 변경된 좌표 -> ", "${location.latitude} : ${location.longitude}") }
////                with(EventBus.getDefault()) {
////                    if (hasSubscriberForEvent(ChangedMyLocationEvent::class.java)) post(
////                        ChangedMyLocationEvent(location)
////                    )
////                }
////                val userInfo = AppUserInfo.getUserInfo(c)
////                userInfo.apply {
////                    put(RestTree.Common.Param.LATITUDE, location.latitude)
////                    put(RestTree.Common.Param.LONGITUDE, location.longitude)
////                }
////                AppUserInfo.setUserInfo(c, userInfo)
////                locList.add(
////                    MLoc(
////                        AppUserInfo.getId(c),
////                        location.latitude,
////                        location.longitude,
////                        System.currentTimeMillis()
////                    )
////                )
////                if (locList.size >= 5) sendLoc()
////                AndroidLocationUtil.latitude = location.latitude
////                AndroidLocationUtil.longitude = location.longitude
////            }
////        }
////
////        override fun onProviderDisabled(provider: String) {
////        }
////
////        override fun onProviderEnabled(provider: String) {
////        }
////
////        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
////        }
////    }
//
//    inner class LocationListener(provider: String) : NMapLocationManager.OnLocationChangeListener {
//        private var mLastLocation: Location
//
//        init {
//            mLastLocation = Location(provider)
//        }
//
//        override fun onLocationChanged(p0: NMapLocationManager?, p1: NGeoPoint?): Boolean {
//            p0 ?: return true
//            p1 ?: return true
//            val c = this@LocationService
//            val location = p0.lastLocationFix
//            if (location.accuracy <= 6.0) {
//                with(EventBus.getDefault()) {
//                    if (hasSubscriberForEvent(ChangedMyLocationEvent::class.java)) post(
//                        ChangedMyLocationEvent(location)
//                    )
//                }
//                val userInfo = AppUserInfo.getUserInfo(c)
//                userInfo.apply {
//                    put(RestTree.Common.Param.LATITUDE, location.latitude)
//                    put(RestTree.Common.Param.LONGITUDE, location.longitude)
//                }
//                AppUserInfo.setUserInfo(c, userInfo)
//                locList.add(
//                    MLoc(
//                        AppUserInfo.getId(c),
//                        location.latitude,
//                        location.longitude,
//                        System.currentTimeMillis()
//                    )
//                )
//                if (locList.size >= 5) sendLoc()
//                AndroidLocationUtil.latitude = location.latitude
//                AndroidLocationUtil.longitude = location.longitude
//            }
//            return true
//        }
//
//        override fun onLocationUnavailableArea(p0: NMapLocationManager?, p1: NGeoPoint?) {
//        }
//
//        override fun onLocationUpdateTimeout(p0: NMapLocationManager?) {
//        }
//    }
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return START_NOT_STICKY
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//
//        val builder = NotificationCompat.Builder(
//            this,
//            getString(R.string.fcmChannelId)
//        )
//            .setSmallIcon(R.drawable.ic_status_noti)
//            .setColor(ColorUtils.getColor(R.color.mainColor))
//            .setContentTitle("GPS")
//            .setContentText("위치 수집 중...")
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
//                NotificationChannel(
//                    getString(R.string.fcmChannelId),
//                    "GPS",
//                    NotificationManager.IMPORTANCE_NONE
//                )
//            )
//        }
//        startForeground(9, builder.build())
//
//        // 위치 정보 수집
//        initializeLocationManager()
//
//        try {
//            mLocationManager?.enableMyLocation(true)
//            mLocationManager?.setOnLocationChangeListener(mLocationListeners[0])
//        } catch (ex: SecurityException) {
//        } catch (ex: IllegalArgumentException) {
//        }
//
//        // 마지막 위치 수집 시간과 현재시간 차이가 길 때 현재 수집된 위치까지 서버에 전송
//        checkJob = runAsync {
//            while (true) {
//                delay(10000L)
//                if (locList.isNotEmpty()) {
//                    val current = System.currentTimeMillis()
//                    val last = locList.last()
//                    if (current - last.time >= 10000) {
//                        sendLoc()
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        stopForeground(true)
//        checkJob?.cancel()
//        checkJob = null
////        if (mLocationManager != null) {
////            try {
////                mLocationManager?.removeUpdates(locationListener)
////                stopForeground(true)
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
////        }
//        val lm = mLocationManager ?: return
//        lm.disableMyLocation()
//        lm.removeOnLocationChangeListener(mLocationListeners[0])
//    }
//
//    /**
//     * 위치 전송하는 로직
//     */
//    private fun sendLoc() {
//        val send = ArrayList(locList)
//        locList.clear()
//        if (send.isNotEmpty())
//            debugTry {
//                runAsync {
//                    AndroidLocationUtil.sendLocation(this, send)
//                }
//            }
//    }
//
//    private fun initializeLocationManager() {
//        if (mLocationManager == null) {
//            mLocationManager = NMapLocationManager(this).apply {
//                setUpdateFrequency(1000L, 0.00001f)
//            }
//        }
//    }
//
////    private fun initializeLocationManager() {
////        if (mLocationManager == null)
////            mLocationManager =
////                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
////        if (mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
////            if (ActivityCompat.checkSelfPermission(
////                    this,
////                    Manifest.permission.ACCESS_FINE_LOCATION
////                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
////                    this,
////                    Manifest.permission.ACCESS_COARSE_LOCATION
////                ) != PackageManager.PERMISSION_GRANTED
////            ) {
////
////                return
////            }
////            mLocationManager?.requestLocationUpdates(
////                LocationManager.GPS_PROVIDER,
////                2000,
////                0.0f,
////                locationListener
////            )
////        }
////    }
//
//}
