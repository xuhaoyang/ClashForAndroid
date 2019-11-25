package com.github.kr328.clash.service.data

import androidx.room.*

@Entity(tableName = "profiles")
@TypeConverters(ClashProfileEntity.Converters::class)
data class ClashProfileEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "token") val token: String,
    @ColumnInfo(name = "cache") val cache: String,
    @ColumnInfo(name = "active") val active: Boolean,
    @ColumnInfo(name = "proxies") val proxies: Int,
    @ColumnInfo(name = "proxy_groups") val proxyGroups: Int,
    @ColumnInfo(name = "rules") val rules: Int,
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int = 0) {

    enum class Type {
        UNKNOWN, URL, FILE
    }

    class Converters {
        @TypeConverter
        fun typeToInt(type: Type?): Int? {
            return type?.run {
                when ( this ) {
                    Type.URL -> 1
                    Type.FILE -> 2
                    Type.UNKNOWN -> 0
                }
            }
        }

        @TypeConverter
        fun intToType(i: Int?): Type? {
            return i?.run {
                when (this) {
                    1 -> Type.URL
                    2 -> Type.FILE
                    else -> Type.UNKNOWN
                }
            }
        }
    }
}