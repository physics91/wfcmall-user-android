package com.theone.busandbt.utils

import android.content.Context
import androidx.room.Room
import com.theone.busandbt.database.*
import com.theone.busandbt.model.BasketListViewModel
import com.theone.busandbt.model.DeliveryAddressViewModel
import com.theone.busandbt.model.LoginInfoViewModel
import com.theone.busandbt.repository.AddressRepository
import com.theone.busandbt.repository.BasketRepository
import com.theone.busandbt.repository.MemberInfoRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object RoomUtils {

    fun module(context: Context): Module = module {
        initBasketModule(context)

        single {
            Room.databaseBuilder(get(), MemberDB::class.java, "member_database")
                .fallbackToDestructiveMigration()
                .build()
        }
        single {
            Room.databaseBuilder(get(), AddressDB::class.java, "address_database")
                .fallbackToDestructiveMigration()
                .build()
        }
        single { get<MemberDB>().memberDao() }
        single { MemberInfoRepository(get()) }
        single { get<AddressDB>().addressDao() }
        single { AddressRepository(get()) }
        viewModel { LoginInfoViewModel(get()).apply { init() } }
        viewModel { DeliveryAddressViewModel() }
    }

    private fun Module.initBasketModule(context: Context) {
        val basketMenuDB = Room.databaseBuilder(context, BasketMenuDB::class.java, "basket_menu_database")
            .fallbackToDestructiveMigration()
            .build()
        val basketShopDB = Room.databaseBuilder(context, BasketShopDB::class.java, "basket_shop_database")
            .fallbackToDestructiveMigration()
            .build()
        val basketMenuOptionDB = Room.databaseBuilder(context, BasketMenuOptionDB::class.java, "basket_menu_option_database")
            .fallbackToDestructiveMigration()
            .build()
        val basketMenuDao = basketMenuDB.basketMenuDao()
        val basketShopDao = basketShopDB.basketShopDao()
        val basketMenuOptionDao = basketMenuOptionDB.basketMenuOptionDao()
        val basketRepository = BasketRepository(basketShopDao, basketMenuDao, basketMenuOptionDao)
        val basketListViewModel = BasketListViewModel(basketRepository)
        basketListViewModel.init()
        single { basketMenuDB }
        single { basketShopDB }
        single { basketMenuDao }
        single { basketShopDao }
        single { basketRepository }
        viewModel { basketListViewModel }
    }
}