package com.github.kr328.clash.service.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClashProfileDao {
    @Query("UPDATE profiles SET active = CASE WHEN id = :id THEN 1 ELSE 0 END")
    fun setSelectedProfile(id: Int)

    @Query("SELECT * FROM profiles WHERE active = 1 LIMIT 1")
    fun observeSelectedProfile(): LiveData<ClashProfileEntity?>

    @Query("SELECT * FROM profiles WHERE active = 1 LIMIT 1")
    fun queryActiveProfile(): ClashProfileEntity?

    @Query("SELECT name FROM profiles WHERE active = 1 LIMIT 1")
    fun observeDefaultProfileName(): LiveData<String?>

    @Query("SELECT * FROM profiles ORDER BY name")
    fun observeProfiles(): LiveData<List<ClashProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfile(profile: ClashProfileEntity)
}