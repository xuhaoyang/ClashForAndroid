package com.github.kr328.clash.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClashProfile(
    @SerialName("Proxy") val proxies: List<ClashProxy>,
    @SerialName("Proxy Group") val proxyGroups: List<ClashProxyGroup>,
    @SerialName("Rule") val rules: List<ClashRule>
)