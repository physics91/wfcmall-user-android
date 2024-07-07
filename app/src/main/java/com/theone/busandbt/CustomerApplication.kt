package com.theone.busandbt

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.kakao.sdk.common.KakaoSdk
import com.theone.busandbt.model.AppViewModel
import com.theone.busandbt.model.JoinInfoViewModel
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import com.theone.busandbt.utils.RetrofitUtils
import com.theone.busandbt.utils.RoomUtils
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * 애플리케이션 설정 객체
 */
class CustomerApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            httpSender {
                uri = BuildConfig.ACRA_URL
                httpMethod = HttpSender.Method.POST
            }
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        KakaoSdk.init(this, getString(R.string.kakaoSdkKey))
        startKoin {
            androidContext(this@CustomerApplication)
            val categoryModel = CategoryViewModel()
            modules(
                RetrofitUtils.loginChannelModule(),
                RetrofitUtils.orderChannelModule(),
                RetrofitUtils.paymentChannelModule(),
                RetrofitUtils.kakaoAPIModule(),
                RoomUtils.module(this@CustomerApplication),
                module {
                    single { JACKSON_OBJECT_MAPPER }
                    viewModel { AppViewModel() }
                    viewModel { categoryModel }
                    viewModel { JoinInfoViewModel() }
                }
            )
            categoryModel.init()
        }
    }
}