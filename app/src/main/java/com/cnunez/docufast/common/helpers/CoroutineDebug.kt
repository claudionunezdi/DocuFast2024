package com.cnunez.docufast.common.helpers



import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class CoroutineDebug(override val key: CoroutineContext.Key<*>) : CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Log.e(
            "COROUTINE_ERROR", """
            |Context: $context
            |Mensaje: ${exception.message}
            |Stacktrace: ${exception.stackTraceToString()}
        """.trimMargin()
        )
    }
}
