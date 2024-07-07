package com.theone.busandbt.api.orderchannel

import com.busandbt.code.ChannelType
import com.busandbt.code.NoticeType
import com.theone.busandbt.dto.notice.NoticeDetail
import com.theone.busandbt.dto.notice.NoticeFixedListItem
import com.theone.busandbt.dto.notice.NoticeListItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 공지 API
 */
interface NoticeAPI {

    /**
     * 공지 리스트 조회
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     * @param type 이 공지 유형에 맞는 공지 리스트를 조회한다.
     */
    @GET("/notice")
    fun getNoticeList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("type") type: Int = NoticeType.MEMBER.id
    ): Call<List<NoticeListItem>>

    /**
     * 공지 상세 조회
     * @param noticeId 공지 고유번호
     */
    @GET("/notice/{noticeId}")
    fun getNoticeDetail(@Path("noticeId") noticeId: Int): Call<NoticeDetail>

    /**
     * 상단 공지 간략 리스트 조회
     */
    @GET("/notice/fixed")
    fun getFixedNoticeList(
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("noticeType") noticeType: Int = NoticeType.MEMBER.id
    ): Call<List<NoticeFixedListItem>>
}