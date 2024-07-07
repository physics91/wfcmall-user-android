package com.theone.busandbt.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.theone.busandbt.item.basket.BasketShopWithoutMenu

@Dao
interface BasketShopDao {
    @Query("SELECT * FROM basket_shop;")
    fun getShopListLiveData(): LiveData<List<BasketShopWithoutMenu>>

    @Query("SELECT * FROM basket_shop;")
    suspend fun getShopListAll(): List<BasketShopWithoutMenu>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(item: BasketShopWithoutMenu): Long

    @Query("DELETE FROM basket_shop WHERE id = :id")
    suspend fun remove(id: Long)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun set(item: BasketShopWithoutMenu)
}