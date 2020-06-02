package com.hearing.ipcbridge

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.os.IBinder

/**
 * @author liujiadong
 * @since 2020/6/1
 */
internal class BinderCursor internal constructor(columnNames: Array<String?>, binder: IBinder) : MatrixCursor(columnNames) {
    private val mBinderExtra = Bundle()

    override fun getExtras(): Bundle {
        return mBinderExtra
    }

    companion object {
        private const val KEY_BINDER = "binder"

        fun getBinder(cursor: Cursor?): IBinder? {
            cursor?.let {
                return it.extras?.getBinder(KEY_BINDER)
            }
            return null
        }
    }

    init {
        mBinderExtra.putBinder(KEY_BINDER, binder)
    }
}