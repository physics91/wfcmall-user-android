package com.theone.busandbt.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.theone.busandbt.item.AddressSaveItem
/**
 * (주소저장) SQL 쿼리를 지정하여 메서드 호출과 연결
 * */
@Dao
interface AddressSaveDao {
    @Query("SELECT *  FROM addressave_table")
    fun readAllData(): LiveData<List<AddressSaveItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(search: AddressSaveItem)

    @Update
    suspend fun updateAddress(address: AddressSaveItem)
}