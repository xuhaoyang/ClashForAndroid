@file:Suppress("DEPRECATION")

package com.github.kr328.clash.common.compat

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.getColorCompat(@ColorRes id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColor(id)
    } else {
        resources.getColor(id)
    }
}

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}