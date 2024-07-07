package com.theone.busandbt.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.theone.busandbt.dao.SearchHistoryDao
import com.theone.busandbt.item.RecentSearchKeyword
/**
 * 최근 검색어 디비에 연결
 * */
@Database(entities = [RecentSearchKeyword::class], version = 1, exportSchema = false)
abstract class SearchHistoryDB : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        /* @Volatile = 접근가능한 변수의 값을 cache를 통해 사용하지 않고
        thread가 직접 main memory에 접근 하게하여 동기화. */
        @Volatile
        private var instance: SearchHistoryDB? = null

        // 싱글톤으로 생성 (자주 생성 시 성능 손해). 이미 존재할 경우 생성하지 않고 바로 반환
        fun getDatabase(context: Context): SearchHistoryDB? {
            if (instance == null) {
                synchronized(SearchHistoryDB::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SearchHistoryDB::class.java,
                        "RoomDb"
                    ).build()
                }
            }
            return instance
        }
    }
}