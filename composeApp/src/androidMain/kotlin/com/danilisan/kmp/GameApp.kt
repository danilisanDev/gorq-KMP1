package com.danilisan.kmp

import android.app.Application
import com.danilisan.kmp.core.dependency.initKoin
import com.danilisan.kmp.core.log.initNapier

class GameApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
        initNapier()
    }

}