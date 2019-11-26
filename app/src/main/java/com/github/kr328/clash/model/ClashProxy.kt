package com.github.kr328.clash.model

import kotlinx.serialization.Serializable

@Serializable
data class ClashProxy(
    private val type: String,
    private val name: String
)