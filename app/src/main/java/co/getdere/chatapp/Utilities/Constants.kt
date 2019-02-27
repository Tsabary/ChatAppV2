package co.getdere.chatapp.Utilities

const val BASE_URL = "https://glacial-cove-54964.herokuapp.com/v1/"
//const val BASE_URL = "http://10.0.2.2:3005/v1/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}user/byemail/"
const val SOCKET_URL = "https://glacial-cove-54964.herokuapp.com/"
const val URL_GET_CHANNELS = "${BASE_URL}channel/"
const val URL_GET_MESSAGES = "${BASE_URL}messageBody/bychannel/"

    //Broadcast constants

const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"