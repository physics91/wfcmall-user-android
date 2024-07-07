package com.theone.busandbt.repository

import androidx.lifecycle.LiveData
import com.theone.busandbt.dao.AddressDao
import com.theone.busandbt.dto.address.DeliveryAddress

/***
 * (주소검색) 앱에서 사용하는 데이터와 그 데이터 통신을 하는 역할
 */
class AddressRepository(private val addressDao: AddressDao) {

    val readAllData: LiveData<List<DeliveryAddress>> = addressDao.readAllData()

    suspend fun getListAll(): List<DeliveryAddress> = addressDao.getListAll()

    suspend fun add(address: DeliveryAddress) {
        val localId = addressDao.insert(address)
        address.id = localId
    }

    suspend fun remove(address: DeliveryAddress) {
        addressDao.delete(address)
    }

    suspend fun set(address: DeliveryAddress) {
        addressDao.update(address)
    }

    suspend fun removeAll() {
        addressDao.deleteAll()
    }
}