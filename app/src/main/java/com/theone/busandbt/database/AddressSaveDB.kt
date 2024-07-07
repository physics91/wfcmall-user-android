package com.theone.busandbt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.AddressSaveDao
import com.theone.busandbt.item.AddressSaveItem

/**
 * 주소저장 디비에 연결
 * */
@Database(entities = [AddressSaveItem::class], version = 1, exportSchema = false)
abstract class AddressSaveDB : RoomDatabase() {
    abstract fun addressDao(): AddressSaveDao
}