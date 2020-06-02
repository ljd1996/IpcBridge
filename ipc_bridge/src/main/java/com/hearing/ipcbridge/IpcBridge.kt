package com.hearing.ipcbridge

import android.content.Context
import android.net.Uri
import java.lang.Exception

/**
 * @author liujiadong
 * @since 2020/6/1
 */
object IpcBridge {
    const val TAG = "IpcBridge"

    private val mClassConfig = mutableMapOf<Class<*>, Class<*>>()
    private val mInstanceConfig = mutableMapOf<Class<*>, Any>()
    private val mProxyCache = mutableMapOf<Class<*>, Any>()

    private var mBridgeInterface: IBridgeInterface? = null

    fun register(inter: Class<*>, impl: Class<*>) {
        mClassConfig[inter] = impl
    }

    fun register(inter: Class<Any>, instance: Any) {
        mInstanceConfig[inter] = instance
    }

    fun getInstance(inter: Class<*>): Any? {
        if (mInstanceConfig.containsKey(inter)) {
            return mInstanceConfig[inter]
        } else if (mClassConfig.containsKey(inter)) {
            val clazz = mClassConfig[inter]
            clazz?.let {
                val o = it.newInstance()
                if (inter.isInstance(o)) {
                    mInstanceConfig[inter] = o
                    return o
                }
            }
        }
        return null
    }

    fun getProxy(context: Context, inter: Class<*>): Any? {
        if (mProxyCache.containsKey(inter)) {
            return mProxyCache[inter] ?: newProxy(context, inter)
        }
        return newProxy(context, inter)
    }

    private fun newProxy(context: Context, inter: Class<*>): Any? {
        if (mBridgeInterface == null) {
            val cursor = context.contentResolver?.query(Uri.parse("content://${BridgeProvider.AUTHORITY}"),
                    null, null, arrayOf(BridgeProvider.SERVICE_IPC), null)
            cursor?.let {
                val binder = BinderCursor.getBinder(it)
                try {
                    mBridgeInterface = IBridgeInterface.Stub.asInterface(binder)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            cursor?.close()
        }
        val proxy = BridgeHandler.newProxyInstance(inter, mBridgeInterface ?: return null)
        mProxyCache[inter] = proxy
        return proxy
    }
}