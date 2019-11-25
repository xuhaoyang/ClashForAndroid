package com.github.kr328.clash.service.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClashProfileDao {
    @Query("UPDATE profiles SET active = CASE WHEN id = :id THEN 1 ELSE 0 END")
    fun setActiveProfile(id: Int)

    @Query("SELECT * FROM profiles WHERE active = 1 LIMIT 1")
    fun queryActiveProfile(): ClashProfileEntity?

    @Query("SELECT * FROM profiles")
    fun queryProfiles(): Array<ClashProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfile(profile: ClashProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    fun removeProfile(id: Int)
}