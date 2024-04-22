package com.vmhatre.dreamcatcher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.vmhatre.dreamcatcher.database.DreamDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val DATABASE_NAME = "dream-database"

@OptIn(DelicateCoroutinesApi::class)
class DreamRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
){

    private val database = Room.databaseBuilder(
        context,
        DreamDatabase::class.java,
        DATABASE_NAME
    ).createFromAsset(DATABASE_NAME)
        .build()

    // for DDF
    suspend fun getDream(id: UUID): Dream{
        return database.dreamDao().getDreamsWithEntries(id)
    }

    fun getDreams(): Flow<List<Dream>> {
        val dreamMapFlow = database.dreamDao().getDreams()
        return dreamMapFlow.map {dreamMap ->
            dreamMap.keys.map {
                    dream -> dream.apply { entries = dreamMap.getValue(dream) }
            }
        }
    }

    fun updateDream(dream: Dream) {
        coroutineScope.launch {
            database.dreamDao().updateDreamWithEntries(dream)
        }

    }

    suspend fun insertDream(dream: Dream){
        database.dreamDao().insertDreamWithEntries(dream)
    }

    suspend fun deleteDream(dream: Dream){
        database.dreamDao().deleteDreamWithEntries(dream)
    }

    companion object {
        private var INSTANCE: DreamRepository? = null

        fun initialize(context: Context) {
            check(INSTANCE == null) {"DreamRepository already initialized"}
            INSTANCE = DreamRepository(context)
        }

        fun get(): DreamRepository {
            return checkNotNull(INSTANCE) {"DreamRepository not yet initialized"}
        }
    }
}