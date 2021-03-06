package com.otnieldocs.rxutilities.permission

import java.lang.Exception

sealed class RxResult <out R>

data class Granted<out T>(val data: T): RxResult<T>()
data class Denied<out T>(val exception: RxPermissionException): RxResult<T>()
data class Failed(val exception: Exception): RxResult<Nothing>()

val RxResult<*>.isSucceed get() = this is Granted && data != null