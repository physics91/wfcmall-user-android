package com.theone.busandbt.utils

/**
 * 최대 메뉴 개수 (하나의 메뉴당 주문할 수 있는)
 */
const val MAX_MENU_COUNT = 99

/**
 * 장바구니에 최대로 담을 수 있는 메뉴의 종류 수를 의미한다.
 */
const val MAX_BASKET_COUNT = 100

/**
 * 최소 메뉴 개수 (하나의 메뉴당 주문할 수 있는)
 * 메뉴를 세팅할때 기본 개수로 사용한다.
 */
const val MIN_MENU_COUNT = 1

/**
 * 앱 설정 키 값
 * SharedPreferences에서 앱 설정의 키 값으로 사용
 */
const val APP_SETTINGS_KEY = "appSettings"

/**
 * 통상적인 한 페이지당 데이터 조회 개수
 */
const val COMMON_DATA_COUNT = 15

/**
 * 리뷰 최대 이미지 등록 개수
 */
const val MAX_REVIEW_IMAGE_COUNT = 3

/**
 * 사용자 이름의 최소 길이를 의미한다.
 */
const val MIN_USER_NAME_LENGTH = 2

/**
 * 휴대폰 번호 길이
 */
const val PHONE_NUMBER_LENGTH = 11

const val EMAIL_REGEX =
    "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}\$"

/**
 * 14세 미만은 회원가입을 하지 못한다.
 */
const val JOIN_LIMIT_AGE = 14

/**
 * 사이다페이 간편비밀번호 길이
 */
const val CARD_SIMPLE_PASSWORD_LENGTH = 6