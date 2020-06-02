package com.hearing.ipcbridge

import android.os.Bundle
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author liujiadong
 * @since 2020/6/1
 */
internal class BridgeHandler(
    private val mClass: Class<*>,
    private var mBridgeInterface: IBridgeInterface
) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        val methodName = method?.name
        args?.get(0)?.let {
            if (it is Bundle) {
                it.putString("clazzName", mClass.name)
                it.putString("methodName", methodName)
                return mBridgeInterface.call(it)
            }
        }
        return method?.invoke(proxy, args)
    }

    companion object {
        fun newProxyInstance(clazz: Class<*>, bridgeInterface: IBridgeInterface): Any {
            val handler = BridgeHandler(clazz, bridgeInterface)
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), handler)
        }
    }
}