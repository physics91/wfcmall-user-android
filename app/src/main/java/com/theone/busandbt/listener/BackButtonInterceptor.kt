package com.theone.busandbt.listener

interface BackButtonInterceptor {

    /**
     * true로 반환할 경우 액티비티에 설정된 백버튼 로직이 실행된다.
     * false로 반환할 경우 기존 백버튼 로직은 무시된다.
     */
    fun interceptOnBackButtonClicked(): Boolean
}