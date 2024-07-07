package com.theone.busandbt.repository

import com.theone.busandbt.dao.MemberInfoDao
import com.theone.busandbt.item.LoginInfo
import kotlinx.coroutines.flow.Flow

/**
 * (유저정보) 앱에서 사용하는 데이터와 그 데이터 통신을 하는 역할
 */
class MemberInfoRepository(private val memberDao: MemberInfoDao) {

    suspend fun getLoginInfo() = memberDao.getLoginInfo()

    suspend fun insert(member: LoginInfo) {
        memberDao.insert(member)
    }

    suspend fun update(member: LoginInfo) {
        memberDao.update(member)
    }

    suspend fun deleteAll() {
        memberDao.deleteAll()
    }

}