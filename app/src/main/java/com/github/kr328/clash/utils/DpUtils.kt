package com.github.kr328.clash.utils

import android.util.TypedValue
import com.github.kr328.clash.MainApplication

val Int.dp: Float
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            MainApplication.instance.resources.displayMetrics
        )
    }