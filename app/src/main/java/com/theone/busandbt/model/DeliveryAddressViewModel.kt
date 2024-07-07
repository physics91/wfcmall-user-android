package com.theone.busandbt.model

import androidx.lifecycle.*
import com.blankj.utilcode.util.SPUtils
import com.theone.busandbt.api.orderchannel.DeliveryAddressAPI
import com.theone.busandbt.dto.address.DeliveryAddress
import com.theone.busandbt.dto.address.request.AddDeliveryAddressRequest
import com.theone.busandbt.extension.callOnSuccess
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.repository.AddressRepository
import com.theone.busandbt.utils.APP_SETTINGS_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

/**
 * 주소 정보 저장 용 데이터 객체
 * 주소 등록,수정,삭제 등 전반적인 기능을 다룬다
 */
class DeliveryAddressViewModel : ViewModel() {
    private val deliveryAddressAPI: DeliveryAddressAPI by inject(DeliveryAddressAPI::class.java)
    private val deliveryAddressLiveData = MutableLiveData<List<DeliveryAddress>>()
    private val repository: AddressRepository by inject(AddressRepository::class.java)
    private val deliveryAddressList = ArrayList<DeliveryAddress>()
    private val selectedAddressLiveData = MutableLiveData<DeliveryAddress?>()
    val selectedDeliveryAddress get() = selectedAddressLiveData.value

    fun observeAddressList(lifecycleOwner: LifecycleOwner, op: (List<DeliveryAddress>) -> Unit) {
        deliveryAddressLiveData.observe(lifecycleOwner) {
            op(it)
        }
    }

    fun observeSelectedAddress(lifecycleOwner: LifecycleOwner, op: (DeliveryAddress?) -> Unit) {
        selectedAddressLiveData.observe(lifecycleOwner) {
            op(it)
        }
    }

    /**
     * 주소 데이터를 초기화한다.
     * 로그인 상태일 경우 서버에서 회원 주소 데이터를 조회해서 처리한다.
     * 비로그인일 경우 로컬 캐시에 있는 주소를 불러온다.
     */
    fun init(token: String? = null, memberId: Int? = null) {
        val isLoginState = token != null && memberId != null
        viewModelScope.launch(Dispatchers.IO) {
            val innerDeliveryAddressList = repository.getListAll()
            launch(Dispatchers.Main) main@{
                if (!isLoginState) {
                    if (deliveryAddressList.isEmpty()) {
                        deliveryAddressList.addAll(innerDeliveryAddressList)
                        deliveryAddressLiveData.value = deliveryAddressList
                        val selected = innerDeliveryAddressList.find { address -> address.enabled }
                        selectedAddressLiveData.value = selected
                    }
                    return@main
                } // 로그인 상태가 아닐경우(로그아웃)
                val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
                if (sp.getBoolean("initAddress") && deliveryAddressList.isEmpty()) {
                    deliveryAddressList.addAll(innerDeliveryAddressList)
                    deliveryAddressLiveData.value = deliveryAddressList
                    val selected = innerDeliveryAddressList.find { address -> address.enabled }
                    selectedAddressLiveData.value = selected
                    return@main
                }
                if (token == null || memberId == null) return@main
                deliveryAddressAPI.getDeliveryAddressList(token, memberId).callOnSuccess {
                    sp.put("initAddress", true)
                    if (it.isEmpty()) { // 이 경우 로컬에 있는 주소들을 사용자에게 추가한다. 그리고 서버에도 저장시킨다.

                        // TODO - 서버에 등록할때 리스트로 한번에 등록하도록 해야함
                        innerDeliveryAddressList.forEach { address ->
                            deliveryAddressAPI.addDeliveryAddress(
                                token,
                                AddDeliveryAddressRequest(
                                    memberId,
                                    address.name,
                                    address.jibun,
                                    address.road,
                                    address.addressDetail,
                                    address.lat,
                                    address.lng,
                                    address.enabled
                                )
                            )
                        }
                        return@callOnSuccess
                    }

                    deliveryAddressList.clear()
                    // 기존 로컬주소를 싹 제거하고 새로운 회원주소를 입력한다.
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.removeAll()
                        addAll(it.map { a -> a.toLocalDeliveryAddress() })
                    }
                }
            }
        }
    }

    fun getAllList(): List<DeliveryAddress> = deliveryAddressList

    /**
     * 주소 등록
     * Room에 주소를 등록 해준다.
     */
    fun add(item: DeliveryAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            val i =
                deliveryAddressList.find { item.name in arrayOf("집", "회사") && it.name == item.name }
            if (i != null) {
                i.jibun = item.jibun
                i.road = item.road
                i.addressDetail = item.addressDetail
                i.lat = item.lat
                i.lng = item.lng
                i.enabled = true
                repository.set(i)
                launch(Dispatchers.Main) {
                    if (item.enabled) { // 추가된 배송주소가 활성화된 경우
                        selectedAddressLiveData.value = item
                    }
                    deliveryAddressLiveData.value = deliveryAddressList
                }
            } else {
                debugLog("배달주소", "새로운 추가 : $item")
                repository.add(item)
                val old = selectedDeliveryAddress
                if (old != null) {
                    debugLog("이전주소", old)
                    old.enabled = false
                    repository.set(old)
                }
                deliveryAddressList.add(item)
                debugLog("배달주소 결과", deliveryAddressList)
                launch(Dispatchers.Main) {
                    if (item.enabled) { // 추가된 배송주소가 활성화된 경우
                        selectedAddressLiveData.value = item
                    }
                    deliveryAddressLiveData.value = deliveryAddressList
                }
            }
        }
    }

    fun addAll(items: List<DeliveryAddress>) {
        debugLog("배달주소 전부추가", items)
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { repository.add(it) }
            launch(Dispatchers.Main) {
                deliveryAddressList.addAll(items)
                val newSelected = items.find { it.enabled }
                if (newSelected != null) { // 추가된 항목들 중 활성화가 된 주소가 있을 경우
                    val selected = selectedDeliveryAddress
                    if (selected != null) { // 이미 활성화된 배송주소가 있는 경우 기존 활성화 배송주소를 비활성화한다.
                        selected.enabled = false
                        set(selected)
                    }
                    selectedAddressLiveData.value = newSelected
                }
                deliveryAddressLiveData.value = deliveryAddressList
            }
        }
    }

    /**
     * 주소 삭제
     * Room에 주소 삭제를 해준다.
     */
    fun remove(item: DeliveryAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.remove(item)
            launch(Dispatchers.Main) {
                deliveryAddressList.remove(item)
                if (selectedDeliveryAddress?.id == item.id) selectedAddressLiveData.value = null
                deliveryAddressLiveData.value = deliveryAddressList
            }
        }
    }

    /**
     * 배송주소 수정
     * TODO - 이 함수가 내부에서 호출될 경우 리스트 알림이 여러번 갈 수 있는 비효율을 없애야한다.
     */
    fun set(item: DeliveryAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.set(item)
            launch(Dispatchers.Main) {
                val index = deliveryAddressList.indexOfFirst { it.id == item.id }
                if (index != -1) deliveryAddressList[index] = item
                val selected = selectedDeliveryAddress
                if (selected?.id != item.id && item.enabled) selectedAddressLiveData.value = item
                deliveryAddressLiveData.value = deliveryAddressList
            }
        }
    }

    fun toggle(address: DeliveryAddress, flag: Boolean) {
        val selected = selectedDeliveryAddress
        if (selected?.id == address.id) return
        if (selected != null) {
            selected.enabled = false
            set(selected)
        }
        address.enabled = flag
        set(address)
    }
}