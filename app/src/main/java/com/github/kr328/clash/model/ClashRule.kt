package com.github.kr328.clash.model

import com.charleskorn.kaml.YamlException
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable(ClashRule.Serializer::class)
data class ClashRule(val matcher: Matcher, val pattern: String, val target: String, val extras: List<String>) {
    enum class Matcher {
        DOMAIN_SUFFIX,
        DOMAIN_KEYWORD,
        DOMAIN,
        IP_CIDR,
        IP_CIDR6,
        SRC_IP_CIDR,
        GEOIP,
        DST_PORT,
        SRC_PORT,
        MATCH;

        override fun toString(): String {
            return when (this) {
                DOMAIN_SUFFIX -> "DOMAIN-SUFFIX"
                DOMAIN_KEYWORD -> "DOMAIN-KEYWORD"
                DOMAIN -> "DOMAIN"
                IP_CIDR -> "IP-CIDR"
                IP_CIDR6 -> "IP-CIDR6"
                SRC_IP_CIDR -> "SRC-IP-CIDR"
                GEOIP -> "GEOIP"
                DST_PORT -> "DST-PORT"
                SRC_PORT -> "SRC-PORT"
                MATCH -> "MATCH"
            }
        }

        companion object {
            fun fromString(s: String): Matcher {
                return when (s) {
                    "DOMAIN-SUFFIX" -> DOMAIN_SUFFIX
                    "DOMAIN-KEYWORD" -> DOMAIN_KEYWORD
                    "DOMAIN" -> DOMAIN
                    "IP-CIDR" -> IP_CIDR
                    "IP-CIDR6" -> IP_CIDR6
                    "SRC-IP-CIDR" -> SRC_IP_CIDR
                    "GEOIP" -> GEOIP
                    "DST-PORT" -> DST_PORT
                    "SRC-PORT" -> SRC_PORT
                    "MATCH" -> MATCH
                    else -> throw YamlException("Invalid matcher $s", 0, 0)
                }
            }
        }
    }

    class Serializer : KSerializer<ClashRule> {
        override val descriptor: SerialDescriptor
            get() = StringDescriptor.withName("rule")

        override fun deserialize(decoder: Decoder): ClashRule {
            val rule = decoder.decodeString().split(",")

            return when (rule.size) {
                0, 1 -> {
                    throw YamlException("Invalid rule $rule", 0, 0)
                }
                2 -> {
                    if (Matcher.fromString(rule[0]) != Matcher.MATCH)
                        throw YamlException("Invalid rule $rule", 0, 0)

                    ClashRule(Matcher.MATCH, "", rule[1], emptyList())
                }
                3 -> {
                    ClashRule(Matcher.fromString(rule[0]), rule[1], rule[2], emptyList())
                }
                else -> {
                    ClashRule(Matcher.fromString(rule[0]), rule[1], rule[2], rule.subList(3, rule.size))
                }
            }
        }

        override fun serialize(encoder: Encoder, obj: ClashRule) {
            if (obj.matcher == Matcher.MATCH) {
                encoder.encodeString("${obj.matcher},${obj.target}")
            } else {
                encoder.encodeString("${obj.matcher},${obj.pattern},${obj.target},${obj.extras.joinToString(",")}")
            }
        }
    }
}