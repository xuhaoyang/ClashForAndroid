package com.github.kr328.clash.common.constants

import com.github.kr328.clash.common.util.packageName

object Intents {
    // Public
    val ACTION_PROVIDE_URL = "$packageName.action.PROVIDE_URL"

    const val EXTRA_NAME = "name"

    // Self
    val ACTION_SERVICE_RECREATED = "$packageName.intent.action.CLASH_RECREATED"
    val ACTION_CLASH_STARTED = "$packageName.intent.action.CLASH_STARTED"
    val ACTION_CLASH_STOPPED = "$packageName.intent.action.CLASH_STOPPED"
    val ACTION_CLASH_REQUEST_STOP = "$packageName.intent.action.CLASH_REQUEST_STOP"
    val ACTION_PROFILE_CHANGED = "$packageName.intent.action.PROFILE_CHANGED"
    val ACTION_PROFILE_REQUEST_UPDATE = "$packageName.intent.action.REQUEST_UPDATE"
    val ACTION_PROFILE_SCHEDULE_UPDATES = "$packageName.intent.action.SCHEDULE_UPDATES"
    val ACTION_PROFILE_LOADED = "$packageName.intent.action.PROFILE_LOADED"
    val ACTION_OVERRIDE_CHANGED = "$packageName.intent.action.OVERRIDE_CHANGED"

    const val EXTRA_STOP_REASON = "stop_reason"
    const val EXTRA_UUID = "uuid"
}