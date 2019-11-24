package com.github.kr328.clash.service;

import com.github.kr328.clash.service.IClashObserver;
import com.github.kr328.clash.core.model.ClashProcessStatus;

interface IClashService {
    void start();
    void stop();
    ClashProcessStatus getClashProcessStatus();
    void registerObserver(String id, boolean notifyCurrent, in IClashObserver observer);
    void unregisterObserver(String id);
    void startTunDevice(in ParcelFileDescriptor fd, int mtu);
    void stopTunDevice();
}
