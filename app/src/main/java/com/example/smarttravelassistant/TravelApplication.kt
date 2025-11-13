package com.example.smarttravelassistant

import android.app.Application
import com.example.smarttravelassistant.model.DatabaseProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        DatabaseProvider.init(this)
    }
}


