package com.github.kr328.clash.service.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, exportSchema = false, entities = [ClashProfileEntity::class, ClashProfileProxyEntity::class])
abstract class ClashDatabase : RoomDatabase() {
    abstract fun openClashProfileDao(): ClashProfileDao
    abstract fun openClashProfileProxyDao(): ClashProfileProxyDao

    companion object {
        private var instance: ClashDatabase? = null

        fun getInstance(context: Context): ClashDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClashDatabase::class.java,
                    "clash-config"
                ).build()
            return instance ?: throw NullPointerException()
        }
    }
}