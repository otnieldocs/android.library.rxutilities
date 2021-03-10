package com.otnieldocs.rxutilities

import java.lang.Exception

class RxPermissionException(
    message: String = "",
    val permissions: List<String> = listOf()
) : Exception(message)