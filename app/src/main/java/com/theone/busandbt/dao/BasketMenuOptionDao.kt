package com.theone.busandbt.dao

import androidx.room.*
import com.theone.busandbt.item.basket.BasketMenuOption

@Dao
interface BasketMenuOptionDao {

    @Query("SELECT * FROM basket_menu_option WHERE basketMenuId = :basketMenuId;")
    suspend fun getList(basketMenuId: Long): List<BasketMenuOption>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(item: BasketMenuOption): Long

    @Query("DELETE FROM basket_menu_option WHERE id = :id;")
    suspend fun remove(id: Long)

    @Query("DELETE FROM basket_menu_option WHERE basketMenuId = :basketMenuId;")
    suspend fun removeAll(basketMenuId: Long)

    @Query("DELETE FROM basket_menu_option WHERE id IN (:idList);")
    suspend fun removeAll(idList: List<Long>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun set(item: BasketMenuOption)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun setAll(itemList: List<BasketMenuOption>)
}