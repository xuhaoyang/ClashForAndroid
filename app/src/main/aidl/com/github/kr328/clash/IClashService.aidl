// IClashService.aidl
package com.github.kr328.clash;

import com.github.kr328.clash.IClashObserver;

interface IClashService {
    void start();
    void stop();
    void registerObserver(String id, boolean notifyCurrent, in IClashObserver observer);
    void unregisterObserver(String id);
    void startTunDevice(in ParcelFileDescriptor fd, int mtu);
    void stopTunDevice();
}
