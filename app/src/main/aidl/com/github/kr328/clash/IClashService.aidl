// IClashService.aidl
package com.github.kr328.clash;

interface IClashService {
    void start();
    void stop();
    void loadProfile(in Uri path);
    void startTunDevice(in ParcelFileDescriptor fd, int mtu);
}
