package com.example.smackchat.utilities

const val BASE_URL = "https://smackchat-bf.herokuapp.com/v1/"
const val SOCKET_URL = "https://smackchat-bf.herokuapp.com/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_ADD_USER = "${BASE_URL}user/add"
const val URL_FIND_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel/"
const val URL_GET_MESSAGES = "${BASE_URL}message/byChannel/"
const val URL_UPLOAD_PHOTO = "${BASE_URL}user/"
const val URL_GET_PHOTO = "https://smackchat-bf.herokuapp.com/uploads/"

const val REQUEST_TIMEOUT = 15000
const val MIN_PASSWORD_LENGTH = 6

const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"

const val SELECT_PROFILE_IMAGE = 1
const val MAX_IMAGE_FILE_SIZE = 100000 //100KB