package com.github.kr328.clash.service.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ClashProfileDao {
    @Query("UPDATE profiles SET selected = CASE WHEN url = :url THEN 1 ELSE 0 END")
    fun setSelectedProfile(url: String)

    @Query("SELECT url FROM profiles WHERE selected = 1 LIMIT 1")
    fun observeDefaultProfileUrl(): LiveData<String?>
}