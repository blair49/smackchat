package com.example.smackchat.utilities

const val BASE_URL = "https://smackchat-bf.herokuapp.com/v1/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_ADD_USER = "${BASE_URL}user/add"
const val URL_FIND_USER = "${BASE_URL}user/byEmail/"

const val REQUEST_TIMEOUT = 15000
const val MIN_PASSWORD_LENGTH = 6

const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"