package com.lihan.qrnest

import android.app.Application
import com.lihan.qrnest.di.initKoin
import org.koin.android.ext.koin.androidContext

class QRNestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@QRNestApp)
        }
    }
}
