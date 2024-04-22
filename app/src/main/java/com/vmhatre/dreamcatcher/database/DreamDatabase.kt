package com.vmhatre.dreamcatcher.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vmhatre.dreamcatcher.Dream
import com.vmhatre.dreamcatcher.DreamEntry

@Database(entities = [Dream::class, DreamEntry::class], version = 1)
@TypeConverters(DreamTypeConverters::class)
abstract class DreamDatabase: RoomDatabase() {

    abstract fun dreamDao(): DreamDao
}