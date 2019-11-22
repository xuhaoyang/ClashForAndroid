package com.github.kr328.clash.model

import kotlinx.serialization.Serializable

@Serializable
data class ClashProxyGroup(
    private val type: String,
    private val name: String
)