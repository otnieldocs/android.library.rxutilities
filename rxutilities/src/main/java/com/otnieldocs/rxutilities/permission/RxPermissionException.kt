package com.otnieldocs.rxutilities.permission

import java.lang.Exception

class RxPermissionException(
    message: String = "",
    val permissions: List<String> = listOf()
) : Exception(message)