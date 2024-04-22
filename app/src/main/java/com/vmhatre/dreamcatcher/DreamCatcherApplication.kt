package com.vmhatre.dreamcatcher

import android.app.Application

class DreamCatcherApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DreamRepository.initialize(this)

    }
}