# IpcBridge

## 概述

- 通过一个AIDL文件实现通用跨进程调用，避免每个IPC接口都需要新建对应的AIDL文件；
- 通过应用ipcbridge的gradle插件，可以通过配置不同进程的信息，从而避免使用Service绑定传递Binder；
- 通过直接注册创建Java/Kotlin接口来进行进程间的接口调用。

## 使用

### 添加依赖和配置

- 需要添加jcenter库

- 在根目录gradle中添加

    ```Groobu
    classpath 'com.hearing.gradle:ipcbridge:1.0.0'
    ```

- 模块gradle中添加

    ```Groovy
    apply plugin: 'com.hearing.gradle.ipcbridge'

    dependencies {
        implementation 'com.hearing:ipcbridge:1.0.0'
    }
    ```

- 配置进程信息，在模块的gradle脚本中添加：

    ```Groovy
    IpcBridge {
        providerConfigs {
            BridgeProvider {
                authorities "com.hearing.ipcbridge.provider"
                process ":bridge"
                exported false
            }
            TestProvider {
                authorities "com.hearing.ipcbridge.test"
                process ":test"
                exported false
            }
        }
    }
    ```

上面配置了两个进程，主要注意process字段的值，需要跟你想要通信的进程的process字段一致。

### 创建Java接口

**创建进程间通信的接口(注意，参数和返回值都是Bundle类型，具体的参数和返回值都约定在Bundle里)：**

```kotlin
interface IBridgeApi {
    fun getName(param: Bundle): Bundle
}
```

**创建接口实现类：**

```kotlin
class BridgeApiImpl : IBridgeApi {
    override fun getName(param: Bundle): Bundle {
        val id = param.getInt("id")
        val result = Bundle()
        result.putString("name", "hearing-$id")
        return result
    }
}
```

### 注册和调用

在子进程中注册接口(:test进程)：

```kotlin
IpcBridge.register(IBridgeApi::class.java, BridgeApiImpl::class.java)
```

在其它进程进程调用：

```kotlin
val bundle = Bundle()
bundle.putInt("id", 100)
// 此处需要指定上面gradle中配置的`:test`进程对应的authorities
val proxy = IpcBridge.getProxy(
    this, IBridgeApi::class.java, "com.hearing.ipcbridge.test"
) as? IBridgeApi
val result = proxy?.getName(bundle)
```

## 原理

项目工程中包括三个模块，一个是IpcBridge的Kotlin源代码，一个是IpcBridge的Gradle插件源码，主模块是demo模块。

主要是通过一个AIDL接口，借助Bundle的特性，用来传递要调用的类和方法名，接下来用A进程表示调用进程，B进程表示被调用进程：

```aidl
interface IBridgeInterface {
    Bundle call(in Bundle args);
}
```

在A进程中通过获取对应接口的代理类，代理类会通过ContentProvider&Binder的方式拿到B进程的Binder对象（不需要使用bindService的方式）：

```kotlin
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
```

代理类则通过这个Binder来调用到B进程的方法：

```kotlin
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
```

## 后续

IpcBridge在配置上还有些麻烦，后面有时间会考虑优化一下，争取实现最少配置。

本人博客：[苍耳的博客](https://ljd1996.github.io)。

相关文章：[Android-IPC机制-ContentProvider](https://ljd1996.github.io/2020/01/06/Android-IPC%E6%9C%BA%E5%88%B6/#ContentProvider-amp-Binder)。
