package com.mobile.travelhub.data

import retrofit2.HttpException

fun Throwable.httpStatusCode(): Int? = (this as? HttpException)?.code()
