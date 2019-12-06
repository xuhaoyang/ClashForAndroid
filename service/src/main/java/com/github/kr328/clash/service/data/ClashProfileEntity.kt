package com.github.kr328.clash.service.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.github.kr328.clash.core.serialization.Parcels
import kotlinx.serialization.Serializable

@Entity(tableName = "profiles")
@Serializable
data class ClashProfileEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "token") val token: String,
    @ColumnInfo(name = "file") val file: String,
    @ColumnInfo(name = "active") val active: Boolean,
    @ColumnInfo(name = "last_update") val lastUpdate: Long,
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcels.dump(serializer(), this, parcel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        fun fileToken(content: String): String {
            return "file|$content"
        }

        fun urlToken(content: String): String {
            return "url|$content"
        }

        fun isFileToken(token: String): Boolean {
            return token.startsWith("file|")
        }

        fun isUrlToken(token: String): Boolean {
            return token.startsWith("url|")
        }

        fun getUrl(token: String): String {
            return token.removePrefix("url|")
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<ClashProfileEntity> {
            override fun createFromParcel(parcel: Parcel): ClashProfileEntity {
                return Parcels.load(serializer(), parcel)
            }

            override fun newArray(size: Int): Array<ClashProfileEntity?> {
                return arrayOfNulls(size)
            }
        }
    }
}