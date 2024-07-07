package com.theone.busandbt.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.theone.busandbt.item.basket.BasketMenuWithoutOption

@Dao
interface BasketMenuDao {
    @Query("SELECT * FROM basket_menu;")
    fun getListLiveData(): LiveData<List<BasketMenuWithoutOption>>

    @Query("SELECT * FROM basket_menu WHERE basketShopId = :basketShopId;")
    suspend fun getList(basketShopId: Long): List<BasketMenuWithoutOption>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(vararg item: BasketMenuWithoutOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: BasketMenuWithoutOption): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAll(vararg item: BasketMenuWithoutOption)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(item: BasketMenuWithoutOption)

    @Delete
    suspend fun removeAll(vararg item: BasketMenuWithoutOption)

    @Query("DELETE FROM basket_menu WHERE basketShopId = :basketShopId")
    suspend fun removeAll(basketShopId: Long)

    @Query("DELETE FROM basket_menu WHERE id = :id;")
    suspend fun remove(id: Long)
}