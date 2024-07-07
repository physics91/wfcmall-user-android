package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.member.response.GetMemberLikeCountResponse
import com.theone.busandbt.dto.shop.*
import com.theone.busandbt.dto.shop.request.ShopLikeToggleRequest
import com.theone.busandbt.dto.shop.response.GetBasketInfoResponse
import com.theone.busandbt.dto.shop.response.GetShopCountResponse
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import com.busandbt.code.*
import retrofit2.Call
import retrofit2.http.*

/**
 * 상점 관련 API들을 정의한다. (주문채널)
 */
interface ShopAPI {

    /**
     * 상점 정보 조회
     * @param shopId 상점의 고유번호
     * @param memberId 회원이 상점을 찜했는지 판단한다. 로그인된 상태에서만 입력한다.
     */
    @GET("/shop/detail/plus/{shopId}")
    fun getShopDetail(
        @Path("shopId") shopId: Int,
        @Query("memberId") memberId: Int? = null
    ): Call<ShopDetail>

    /**
     * 쇼핑몰 정보 조회
     * @param shopId 상점의 고유번호
     * @param memberId 회원이 상점을 찜했는지 판단한다. 로그인된 상태에서만 입력한다.
     */
    @GET("/shop/detail/mall/{shopId}")
    fun getMallShopDetail(
        @Path("shopId") shopId: Int,
        @Query("memberId") memberId: Int? = null
    ): Call<MallShopDetail>

    /**
     * 상점 상세 정보 조회
     * @param shopId 상점의 고유번호
     */
    @GET("/shop/v2/info/{shopId}")
    fun getShopInfo(@Path("shopId") shopId: Int): Call<ShopInfo>

    /**
     * 상점 리스트 조회
     * 음식점, 전통시장 리스트 화면에서 사용한다.
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     * @param jibun 회원의 배송지 지번 주소
     * @param keyword 상점명, 메뉴명으로 검색할 때 입력한다.
     * @param shopSortType 정렬기준이 필요할 때 입력한다. [ShopSortType]
     * @param minOrderCost 최소주문금액으로 필터링할 때 입력한다.
     * @param categoryId 카테고리(한식, 중식 등)로 필터링할 때 입력한다.
     * @param distance 거리별로 정렬이 필요할 때 입력한다. [customerLat], [customerLng]와 같이 입력해야한다.
     * @param customerLat 거리별 정렬 위치 기준(위도)
     * @param customerLng 거리별 정렬 위치 기준(경도)
     * @param coupon 쿠폰있는 상점 필터링
     * @param packaging 포장가능한 상점 필터링
     */
    @GET("/shop/temp")
    fun getShopList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("jibun") jibun: String? = null,
        @Query("shopType") shopType: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("shopSortType") shopSortType: Int? = null,
        @Query("minOrderCost") minOrderCost: Int? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("distance") distance: Double? = null,
        @Query("customerLat") customerLat: Double? = null,
        @Query("customerLng") customerLng: Double? = null,
        @Query("coupon") coupon: Boolean? = null,
        @Query("packaging") packaging: Boolean? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("serviceType") serviceType: Int? = null,
    ): Call<List<ShopListItem>>

    @GET("/shop/count")
    fun getShopCount(
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("jibun") jibun: String? = null,
        @Query("shopType") shopType: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("minOrderCost") minOrderCost: Int? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("distance") distance: Double? = null,
        @Query("customerLat") customerLat: Double? = null,
        @Query("customerLng") customerLng: Double? = null,
        @Query("coupon") coupon: Boolean? = null,
        @Query("packaging") packaging: Boolean? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("serviceType") serviceType: Int? = null,
    ): Call<GetShopCountResponse>

    /**
     * 상점 즐겨찾기 ON/OFF
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/shop/like/{shopId}")
    fun toggleLike(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: Int,
        @Body request: ShopLikeToggleRequest
    ): Call<Unit>

    @GET("/shop/like/count/{memberId}")
    fun getMemberLikeCount(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: Int,
        @Query("serviceType") serviceType: Int? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<GetMemberLikeCountResponse>

    /**
     * 위치를 기준으로 상점 위치 리스트 조회
     * @param centerLat 중심점 위도
     * @param centerLng 중심점 경도
     * @param distance 거리 (중심점 기준으로 어디까지 조회를 할지)
     * @param serviceType 서비스 유형 [ServiceType]
     */
    @GET("/shop/location")
    fun getShopLocationList(
        @Query("centerLat") centerLat: Double,
        @Query("centerLng") centerLng: Double,
        @Query("jibun") jibun: String,
        @Query("distance") distance: Double,
        @Query("serviceType") serviceType: Int? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("hasCoupon") hasCoupon: Boolean? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<ShopLocation>>

    @GET("/shop/like/food")
    fun getLikedFoodList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("memberId") memberId: Int,
        @Query("deliveryType") deliveryType: Int,
        @Query("likeSortType") likeSortType: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<LikedFoodListItem>>

    @GET("/shop/like/mall")
    fun getMallLikeShopList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("memberId") memberId: Int,
        @Query("likeSortType") likeSortType: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<MallLikeListItem>>

    /**
     * 할인상점 리스트 조회
     */
    @GET("/shop/discount")
    fun getDiscountShopList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("jibun") jibun: String,
        @Query("serviceType") serviceType: Int,
        @Query("deliveryType") deliveryType: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<DiscountShopListItem>>

    /**
     * 신규상점 리스트 조회
     */
    @GET("/shop/new")
    fun getNewShopList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("jibun") jibun: String,
        @Query("serviceType") serviceType: Int,
        @Query("deliveryType") deliveryType: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<NewShopListItem>>

    /**
     * 인기맛집 리스트 조회
     */
    @GET("/shop/popular")
    fun getPopularShopList(
        @Query("jibun") jibun: String,
        @Query("serviceType") serviceType: Int,
        @Query("deliveryType") deliveryType: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<PopularShopListItem>>

    /**
     * 장바구니 정보 조회
     */
    @GET("/shop/basket/info/fixed")
    fun getBasketInfo(
        @Query("jibun") jibun: String,
        @Query("customerLat") customerLat: Double,
        @Query("customerLng") customerLng: Double,
        @Query("shopIdList") shopIdList: String,
        @Query("menuIdList") menuIdList: String,
        @Query("optionIdList") optionIdList: String
    ): Call<GetBasketInfoResponse>

    /**
     * 장바구니 정보 조회
     */
    @GET("/shop/packaging")
    fun getPackagingShopInfoList(
        @Query("shopIdList") shopIdList: List<Int>
    ): Call<List<PackagingShopInfo>>

    @DELETE("/shop/like/{memberId}")
    fun removeLikedShopList(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: Int,
        @Query("shopIdList") shopIdList: List<Int>
    ): Call<Unit>
}