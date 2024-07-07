package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.menu.*
import com.busandbt.code.ChannelType
import com.theone.busandbt.dto.menu.response.GetMenuCountResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 메뉴 API
 */
interface MenuAPI {

    /**
     * 음식점 상품/메뉴 리스트 조회
     */
    @GET("/menu/v2/food")
    fun getFoodMenuList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("jibun") jibun: String,
        @Query("shopId") shopId: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("shopSortType") shopSortType: Int? = null,
        @Query("coupon") coupon: Boolean? = null,
    ): Call<List<FoodMenuListItem>>

    /**
     * 메뉴 상세 조회
     */
    @GET("/menu/detail/{menuId}")
    fun getMenuDetail(
        @Path("menuId") menuId: Int
    ): Call<MenuDetail>

    /**
     * 쇼핑몰 상품/메뉴 상세 조회
     */
    @GET("/menu/v2/detail/mall/{menuId}")
    fun getMallMenuDetail(
        @Path("menuId") menuId: Int
    ): Call<MallMenuDetail>

    /**
     * 쇼핑몰 상품/메뉴 리스트 조회
     */
    @GET("/menu/v2/mall")
    fun getMallMenuList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("categoryId") categoryId: Int? = null,
        @Query("jibun") jibun: String,
        @Query("shopId") shopId: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("menuSortType") menuSortType: Int? = null,
        @Query("coupon") coupon: Boolean? = null,
    ): Call<List<MallMenuListItem>>

    /**
     * 쇼핑몰 신규 상품/메뉴 조회
     */
    @GET("/menu/v2/mall/new")
    fun getNewMallMenuList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("jibun") jibun: String,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
    ): Call<List<NewAndPopularMallMenuListItem>>

    /**
     * 쇼핑몰 인기상품/메뉴 리스트 조회
     * 10개로 고정적으로 조회한다.
     */
    @GET("/menu/v2/mall/popular")
    fun getPopularMallMenuList(
        @Query("jibun") jibun: String,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
    ): Call<List<NewAndPopularMallMenuListItem>>

    @GET("/menu/count")
    fun getMenuCount(
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("serviceType") serviceType: Int,
        @Query("jibun") jibun: String,
        @Query("upperShopId") upperShopId: Int? = null,
        @Query("shopId") shopId: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("minOrderCost") minOrderCost: Int? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("distance") distance: Double? = null,
        @Query("customerLat") customerLat: Double? = null,
        @Query("customerLng") customerLng: Double? = null,
        @Query("coupon") coupon: Boolean? = null,
    ): Call<GetMenuCountResponse>
}