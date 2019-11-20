package com.github.kr328.clash.service.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ClashProfileDao {
    @Query("UPDATE profiles SET `default` = CASE WHEN url = :url THEN 1 ELSE 0 END")
    fun setDefaultProfile(url: String)

    @Query("SELECT url FROM profiles WHERE `default` = 1 LIMIT 1")
    fun observeDefaultProfileUrl(): LiveData<String?>
}