package com.miracl.trust.util.json

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.MalformedJsonException
import org.json.JSONException
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

internal object GsonJsonUtil : JsonUtil {
    private const val EMPTY_JSON_STRING_ERROR = "Empty JSON string."

    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(KotlinNonOptionalTypeAdapterFactory())
        .excludeFieldsWithoutExposeAnnotation()
        .create()

    override fun <T : Any> fromJsonString(jsonString: String, clazz: KClass<T>): T {
        try {
            return gson.fromJson(jsonString, clazz.java)
                ?: throw JSONException(
                    EMPTY_JSON_STRING_ERROR
                )
        } catch (ex: JsonSyntaxException) {
            throw JSONException(ex.message)
        } catch (ex: MalformedJsonException) {
            throw JSONException(ex.message)
        } catch (ex: IllegalStateException) {
            throw JSONException(ex.message)
        }
    }

    override fun <T : Any> toJson(obj: T): String {
        return gson.toJson(obj)
    }
}

internal class KotlinNonOptionalTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        val delegate = gson.getDelegateAdapter(this, type)

        // Check if the class is a Kotlin class
        if (type.rawType.declaredAnnotations.none { it.annotationClass.qualifiedName == "kotlin.Metadata" }) {
            return null
        }

        return object : TypeAdapter<T>() {

            override fun write(out: JsonWriter, value: T?) = delegate.write(out, value)

            override fun read(input: JsonReader): T? {

                val value: T? = delegate.read(input)

                if (value != null) {

                    val kotlinClass: KClass<Any> = Reflection.createKotlinClass(type.rawType)

                    // Throws if non-nullable json fields are missing and/or their value is empty or blank
                    kotlinClass.memberProperties.forEach {
                        if (!it.returnType.isMarkedNullable && it.get(value) == null) {
                            throw JSONException("[${it.name}] is missing or null.")
                        }

                        if (it.get(value) is String && (it.get(value) as String).isBlank()) {
                            throw JSONException("${it.name} is null, empty or blank.")
                        }
                    }
                }

                return value
            }
        }
    }
}