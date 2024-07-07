package com.theone.busandbt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.MemberInfoDao
import com.theone.busandbt.item.LoginInfo

/**
 * 유저정보 디비에 연결
 * */
@Database(entities = [LoginInfo::class], version = 2, exportSchema = false)
abstract class MemberDB : RoomDatabase() {
    abstract fun memberDao(): MemberInfoDao
}