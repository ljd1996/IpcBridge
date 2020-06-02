package com.hearing.ipcbridge

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.hearing.ipcbridge.IpcBridge.TAG

/**
 * @author liujiadong
 * @since 2020/6/1
 */
internal class BridgeProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.hearing.ipcbridge.provider"
        const val SERVICE_IPC = "ipc"
    }

    override fun onCreate(): Boolean {
        return false
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (selectionArgs == null || selectionArgs.isEmpty()) {
            return null
        }
        return if (selectionArgs[0] == SERVICE_IPC) {
            BinderCursor(arrayOf("service"), BridgeBinder())
        } else null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    class BridgeBinder : IBridgeInterface.Stub() {
        override fun call(args: Bundle?): Bundle? {
            args?.let {
                val clazzName = it.getString("clazzName") ?: ""
                val methodName = it.getString("methodName") ?: ""
                Log.i(TAG, "clazzName = $clazzName, methodName = $methodName")
                try {
                    val clazz = Class.forName(clazzName)
                    val method = clazz.getMethod(methodName, Bundle::class.java)

                    val instance = IpcBridge.getInstance(clazz)
                    instance?.let { inst ->
                        val result = method.invoke(inst, it)
                        return if (result is Bundle) result else null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }
    }
}