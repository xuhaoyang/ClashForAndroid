package com.github.kr328.clash.service;

import com.github.kr328.clash.service.IClashEventObserver;
import com.github.kr328.clash.service.IClashEventService;
import com.github.kr328.clash.service.IClashProfileService;
import com.github.kr328.clash.service.IClashSettingService;
import com.github.kr328.clash.core.event.Event;
import com.github.kr328.clash.core.model.ProxyPacket;

interface IClashService {
    // Services
    IClashEventService getEventService();
    IClashProfileService getProfileService();
    IClashSettingService getSettingService();

    // Status
    ProcessEvent getCurrentProcessStatus();

    // Control
    void setSelectProxy(String proxy, String selected);
    void startTunDevice(in ParcelFileDescriptor fd, int mtu);
    void stopTunDevice();
    void start();
    void stop();

    // Query
    ProxyPacket queryAllProxies();
}
