package br.com.fiap.airtoday

import android.app.Application

class AppContext : Application() {

    companion object {
        lateinit var instance: AppContext
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
