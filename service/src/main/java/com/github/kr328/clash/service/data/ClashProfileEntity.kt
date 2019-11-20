package com.github.kr328.clash.service.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "profiles", primaryKeys = ["url"])
data class ClashProfileEntity(@ColumnInfo(name = "url") val url: String,
                              @ColumnInfo(name = "default") val defaultProfile: Boolean,
                              @ColumnInfo(name = "writable") val writable: Boolean,
                              @ColumnInfo(name = "last_update") val lastUpdate: Long)