package com.theone.busandbt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.BasketShopDao
import com.theone.busandbt.item.basket.BasketShopWithoutMenu

/**
 * 유저정보 디비에 연결
 * */
@Database(entities = [BasketShopWithoutMenu::class], version = 1, exportSchema = false)
abstract class BasketShopDB : RoomDatabase() {
    abstract fun basketShopDao(): BasketShopDao
}