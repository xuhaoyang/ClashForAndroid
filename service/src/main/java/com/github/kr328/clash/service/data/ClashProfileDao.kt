package com.github.kr328.clash.service.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ClashProfileDao {
    @Query("UPDATE profiles SET selected = CASE WHEN id = :id THEN 1 ELSE 0 END")
    fun setSelectedProfile(id: Long)

    @Query("SELECT cache FROM profiles WHERE selected = 1 LIMIT 1")
    fun observeDefaultProfileCache(): LiveData<String?>

    @Query("SELECT name FROM profiles WHERE selected = 1 LIMIT 1")
    fun observeDefaultProfileName(): LiveData<String?>

    @Query("SELECT * FROM profiles ORDER BY name")
    fun observeProfiles(): LiveData<List<ClashProfileEntity>>
}