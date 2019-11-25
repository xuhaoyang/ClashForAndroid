package com.github.kr328.clash.core.utils

import com.github.kr328.clash.core.Constants.TAG

object Log {
    var handler: LogHandler = object: LogHandler {
        override fun info(message: String, throwable: Throwable?) {
            android.util.Log.i(TAG, message, throwable)
        }

        override fun warn(message: String, throwable: Throwable?) {
            android.util.Log.w(TAG, message, throwable)
        }

        override fun error(message: String, throwable: Throwable?) {
            android.util.Log.e(TAG, message, throwable)
        }

        override fun wtf(message: String, throwable: Throwable?) {
            android.util.Log.wtf(TAG, message, throwable)
        }

        override fun debug(message: String, throwable: Throwable?) {
            android.util.Log.d(TAG, message, throwable)
        }
    }

    interface LogHandler {
        fun info(message: String, throwable: Throwable?)
        fun warn(message: String, throwable: Throwable?)
        fun error(message: String, throwable: Throwable?)
        fun wtf(message: String, throwable: Throwable?)
        fun debug(message: String, throwable: Throwable?)
    }

    fun i(message: String, throwable: Throwable? = null) = handler.info(message, throwable)
    fun w(message: String, throwable: Throwable? = null) = handler.warn(message, throwable)
    fun e(message: String, throwable: Throwable? = null) = handler.error(message, throwable)
    fun wtf(message: String, throwable: Throwable? = null) = handler.wtf(message, throwable)
    fun debug(message: String, throwable: Throwable? = null) = handler.debug(message, throwable)
}
