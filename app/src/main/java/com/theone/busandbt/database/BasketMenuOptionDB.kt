package com.theone.busandbt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.BasketMenuOptionDao
import com.theone.busandbt.item.basket.BasketMenuOption

/**
 * 유저정보 디비에 연결
 * */
@Database(entities = [BasketMenuOption::class], version = 1, exportSchema = false)
abstract class BasketMenuOptionDB : RoomDatabase() {
    abstract fun basketMenuOptionDao(): BasketMenuOptionDao
}