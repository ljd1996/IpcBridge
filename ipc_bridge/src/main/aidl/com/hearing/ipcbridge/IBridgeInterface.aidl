// IBridgeInterface.aidl
package com.hearing.ipcbridge;

import android.os.Bundle;

interface IBridgeInterface {
    Bundle call(in Bundle args);
}
