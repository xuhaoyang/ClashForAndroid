package com.github.kr328.clash.service.data

import androidx.room.*

@Entity(tableName = "profile_select_proxies",
    indices = [Index("profile_id")],
    foreignKeys = [ForeignKey(entity = ClashProfileEntity::class,
        childColumns = ["profile_id"],
        parentColumns = ["id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE)])
data class ClashProfileProxyEntity(
    @ColumnInfo(name = "profile_id") val profileId: Int,
    @ColumnInfo(name = "proxy") val proxy: String,
    @ColumnInfo(name = "selected") val selected: String,
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int = 0
)