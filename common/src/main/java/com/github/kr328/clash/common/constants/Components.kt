package com.github.kr328.clash.common.constants

import android.content.ComponentName
import com.github.kr328.clash.common.util.packageName

object Components {
    val MAIN_ACTIVITY = ComponentName(packageName, "$packageName.MainActivity")
    val PROPERTIES_ACTIVITY = ComponentName(packageName, "$packageName.PropertiesActivity")
}