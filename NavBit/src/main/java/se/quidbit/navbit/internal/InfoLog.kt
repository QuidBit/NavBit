package se.quidbit.navbit.internal

import android.util.Log

object InfoLog {

    var LOGGING_ENABLED: Boolean = false

    fun e(text: String) {
        if (LOGGING_ENABLED) {
            Log.e("NavBit", text)
        }
    }

    fun d(text: String) {
        if (LOGGING_ENABLED) {
            Log.d("NavBit", text)
        }
    }

    fun i(text: String) {
        if (LOGGING_ENABLED) {
            Log.i("NavBit", text)
        }
    }
}
