package com.github.kr328.clash.service;

import com.github.kr328.clash.service.IClashEventObserver;
import com.github.kr328.clash.service.IClashEventService;
import com.github.kr328.clash.core.event.Event;

interface IClashService {
    IClashEventService getEventService();
    void startTunDevice(in ParcelFileDescriptor fd, int mtu);
    void stopTunDevice();
    void start();
    void stop();
}
