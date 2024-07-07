package com.theone.busandbt.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.theone.busandbt.BuildConfig
import com.theone.busandbt.api.loginchannel.CertAPI
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.api.loginchannel.VersionAPI
import com.theone.busandbt.api.orderchannel.CardAPI
import com.theone.busandbt.api.orderchannel.CategoryAPI
import com.theone.busandbt.api.orderchannel.CostAPI
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.api.orderchannel.DeliveryAddressAPI
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.api.orderchannel.NoticeAPI
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.api.orderchannel.PromotionAPI
import com.theone.busandbt.api.orderchannel.ReviewAPI
import com.theone.busandbt.api.orderchannel.SearchAPI
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.api.outside.KakaoAPI
import com.theone.busandbt.api.payment.PaymentAPI
import com.theone.busandbt.jackson.LocalDateDeserializer
import com.theone.busandbt.jackson.LocalDateSerializer
import com.theone.busandbt.jackson.LocalDateTimeDeserializer
import com.theone.busandbt.jackson.LocalDateTimeSerializer
import com.theone.busandbt.jackson.LocalTimeDeserializer
import com.theone.busandbt.jackson.LocalTimeSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Retrofit과 관련된 기능을 정의한다.
 */
object RetrofitUtils {

    private val JACKSON_OBJECT_MAPPER = ObjectMapper().apply {
        registerModule(
            KotlinModule.Builder().build()
                .addSerializer(LocalDateTimeSerializer)
                .addSerializer(LocalDateSerializer)
                .addSerializer(LocalTimeSerializer)
                .addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer)
                .addDeserializer(LocalDate::class.java, LocalDateDeserializer)
                .addDeserializer(LocalTime::class.java, LocalTimeDeserializer)
        )
    }

    /**
     * 공통으로 사용할 jackson converter
     * json 전환객체로 코틀린 data class를 이용하려면 코틀린 모듈 설정이 필요하다.
     */
    private val JACKSON_CONVERTER: JacksonConverterFactory =
        JacksonConverterFactory.create(JACKSON_OBJECT_MAPPER)

    /**
     * 로그인 채널 API koin 모듈
     * API 등록순서는 패키지 정렬순서와 동일하게 맞출것
     */
    fun loginChannelModule() = module {
        val retrofit = createRetrofit(BuildConfig.LOGIN_CHANNEL_BASE_URL)
        single<CertAPI> { retrofit.create(CertAPI::class.java) }
        single<LoginAPI> { retrofit.create(LoginAPI::class.java) }
        single<VersionAPI> { retrofit.create(VersionAPI::class.java) }
    }

    /**
     * 주문 채널 API koin 모듈
     * API 등록순서는 패키지 정렬순서와 동일하게 맞출것
     */
    fun orderChannelModule() = module {
        val retrofit = createRetrofit(BuildConfig.ORDER_CHANNEL_BASE_URL)
        single<CardAPI> { retrofit.create(CardAPI::class.java) }
        single<CategoryAPI> { retrofit.create(CategoryAPI::class.java) }
        single<CostAPI> { retrofit.create(CostAPI::class.java) }
        single<CouponAPI> { retrofit.create(CouponAPI::class.java) }
        single<DeliveryAddressAPI> { retrofit.create(DeliveryAddressAPI::class.java) }
        single<MemberAPI> { retrofit.create(MemberAPI::class.java) }
        single<MenuAPI> { retrofit.create(MenuAPI::class.java) }
        single<NoticeAPI> { retrofit.create(NoticeAPI::class.java) }
        single<OrderAPI> { retrofit.create(OrderAPI::class.java) }
        single<PromotionAPI> { retrofit.create(PromotionAPI::class.java) }
        single<ReviewAPI> { retrofit.create(ReviewAPI::class.java) }
        single<ShopAPI> { retrofit.create(ShopAPI::class.java) }
        single<SearchAPI> { retrofit.create(SearchAPI::class.java) }
    }

    fun paymentChannelModule() = module {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BuildConfig.PAYMENT_CHANNEL_BASE_URL)
//            .build()
        val retrofit = createRetrofit(BuildConfig.PAYMENT_CHANNEL_BASE_URL)
        single<PaymentAPI> { retrofit.create(PaymentAPI::class.java) }
    }

    fun kakaoAPIModule() = module {
        val retrofit = createRetrofit("https://dapi.kakao.com/")
        single<KakaoAPI> { retrofit.create(KakaoAPI::class.java) }
    }

    /**
     * Retrofit을 공통 규격으로 생성한다.
     * @param baseUrl HTTP 통신할 서버주소를 입력한다.
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        val builder = Retrofit.Builder()
        if (BuildConfig.DEBUG) {
            builder.client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build())
        }
        return builder
            .baseUrl(baseUrl)
            .addConverterFactory(JACKSON_CONVERTER)
            .build()
    }
}