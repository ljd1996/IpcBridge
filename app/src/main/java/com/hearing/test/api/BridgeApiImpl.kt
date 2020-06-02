package com.hearing.test.api

import android.os.Bundle

/**
 * @author liujiadong
 * @since 2020/6/1
 */
class BridgeApiImpl : IBridgeApi {
    override fun getName(param: Bundle): Bundle {
        val id = param.getInt("id")
        val result = Bundle()
        result.putString("name", "hearing-$id")
        return result
    }
}