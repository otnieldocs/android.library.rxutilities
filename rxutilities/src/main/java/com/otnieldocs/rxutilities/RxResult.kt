package com.otnieldocs.rxutilities

import java.lang.Exception

sealed class RxResult <out R>

data class Success<out T>(val data: T): RxResult<T>()
data class Failed(val exception: Exception): RxResult<Nothing>()
data class Error(val exception: Exception): RxResult<Exception>()

val RxResult<*>.isSucceed get() = this is Success && data != null