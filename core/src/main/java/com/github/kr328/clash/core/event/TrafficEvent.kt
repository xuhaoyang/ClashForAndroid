package com.github.kr328.clash.core.event

import kotlinx.serialization.Serializable

@Serializable
data class TrafficEvent(val down: Long, val up: Long, val total: Long): Event