package com.miracl.trust.util.json

import kotlin.reflect.KClass

internal interface JsonUtil {
    fun <T : Any> fromJsonString(jsonString: String, clazz: KClass<T>): T

    fun <T : Any> toJson(obj: T): String
}
