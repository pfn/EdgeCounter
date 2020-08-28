package com.hanhuy.edge.counter

import com.samsung.android.sdk.look.Slook

/**
 * @author pfnguyen
 */
class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Slook().initialize(this)
    }
}