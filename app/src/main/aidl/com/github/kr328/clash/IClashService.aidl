// IClashService.aidl
package com.github.kr328.clash;

interface IClashService {
    void loadProfile(String path);
    void startTunDevice(int fd, int mtu);
}
