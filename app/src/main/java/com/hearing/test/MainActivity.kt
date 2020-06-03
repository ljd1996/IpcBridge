package com.hearing.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hearing.ipcbridge.IpcBridge
import com.hearing.test.api.IBridgeApi

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, TestService::class.java))
        window?.decorView?.postDelayed({
            val bundle = Bundle()
            bundle.putInt("id", 100)
            val proxy = IpcBridge.getProxy(
                this,
                IBridgeApi::class.java,
                "com.hearing.ipcbridge.test"
            ) as? IBridgeApi
            val result = proxy?.getName(bundle)
            Log.d("Bridge", "name = ${result?.getString("name")}")
        }, 5000)
    }
}
