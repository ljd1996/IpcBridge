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

    private val mClassMap = mutableMapOf<Class<*>, Class<*>>()
    private val mInstanceMap = mutableMapOf<Class<*>, Any>()
    private val mProxyCache = mutableMapOf<Class<*>, Any>()

    private var mBridgeInterface: IBridgeInterface? = null

    fun register(inter: Class<*>, impl: Class<*>) {
        mClassMap[inter] = impl
    }

    fun register(inter: Class<Any>, instance: Any) {
        mInstanceMap[inter] = instance
    }

    fun getInstance(inter: Class<*>): Any? {
        if (mInstanceMap.containsKey(inter)) {
            return mInstanceMap[inter]
        } else if (mClassMap.containsKey(inter)) {
            val clazz = mClassMap[inter]
            clazz?.let {
                val o = it.newInstance()
                if (inter.isInstance(o)) {
                    mInstanceMap[inter] = o
                    return o
                }
            }
        }
        return null
    }

    fun getProxy(context: Context, inter: Class<*>, authority: String): Any? {
        if (mProxyCache.containsKey(inter)) {
            return mProxyCache[inter] ?: newProxy(context, inter, authority)
        }
        return newProxy(context, inter, authority)
    }

    private fun newProxy(context: Context, inter: Class<*>, authority: String): Any? {
        if (mBridgeInterface == null) {
            val cursor = context.contentResolver?.query(
                Uri.parse("content://${authority}"), null, null, null, null
            )
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