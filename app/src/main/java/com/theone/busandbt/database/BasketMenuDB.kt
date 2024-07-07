package com.theone.busandbt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.BasketMenuDao
import com.theone.busandbt.item.basket.BasketMenuWithoutOption

/**
 * 유저정보 디비에 연결
 * */
@Database(entities = [BasketMenuWithoutOption::class], version = 3, exportSchema = false)
abstract class BasketMenuDB : RoomDatabase() {
    abstract fun basketMenuDao(): BasketMenuDao
}