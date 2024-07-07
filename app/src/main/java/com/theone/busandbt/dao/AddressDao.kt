package com.theone.busandbt.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.theone.busandbt.dto.address.DeliveryAddress

/**
 * (주소검색) SQL 쿼리를 지정하여 메서드 호출과 연결
 * */
@Dao
interface AddressDao {
    @Query("SELECT * FROM address_table;")
    fun readAllData(): LiveData<List<DeliveryAddress>>

    @Query("SELECT * FROM address_table WHERE memberId = :memberId;")
    suspend fun getListAll(memberId: Int): List<DeliveryAddress>

    @Query("SELECT * FROM address_table;")
    suspend fun getListAll(): List<DeliveryAddress>

    @Query("SELECT * FROM address_table WHERE memberId = :memberId;")
    fun loadSingle(memberId: Int): LiveData<List<DeliveryAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: DeliveryAddress): Long

    @Delete
    suspend fun delete(address: DeliveryAddress)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(address: DeliveryAddress)

    @Query("DELETE FROM address_table where id = (select max(id) from address_table)")
    suspend fun deleteLastData()

    @Query("DELETE FROM address_table;")
    suspend fun deleteAll()
}
