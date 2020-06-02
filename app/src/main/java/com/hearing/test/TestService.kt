package com.hearing.test

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.hearing.ipcbridge.IpcBridge

/**
 * @author liujiadong
 * @since 2020/6/2
 */
class TestService : Service() {

    override fun onCreate() {
        super.onCreate()
        IpcBridge.register(
            IBridgeApi::class.java,
            BridgeApiImpl::class.java
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}