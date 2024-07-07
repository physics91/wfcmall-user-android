package com.theone.busandbt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.AddressDao
import com.theone.busandbt.dto.address.DeliveryAddress

/**
 * 주소검색 디비에 연결
 * */
@Database(entities = [DeliveryAddress::class], version = 1, exportSchema = false)
abstract class AddressDB : RoomDatabase() {
    abstract fun addressDao(): AddressDao
}