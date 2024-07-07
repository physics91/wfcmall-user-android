package com.theone.busandbt.dao

import androidx.room.*
import com.theone.busandbt.item.LoginInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberInfoDao {
    @Query("SELECT * FROM login_info LIMIT 1;")
    fun getLoginInfoFlow(): Flow<LoginInfo>

    @Query("SELECT * FROM login_info LIMIT 1;")
    suspend fun getLoginInfo(): LoginInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loginInfo: LoginInfo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(loginInfo: LoginInfo)

    @Query("DELETE FROM login_info")
    suspend fun deleteAll()
}