package com.github.kr328.clash.service;

import com.github.kr328.clash.service.IClashEventObserver;

interface IClashEventService {
    void registerEventObserver(String id, in IClashEventObserver observer);
    void unregisterEventObserver(String id);

    void acquireEvent(String id, int event);
    void releaseEvent(String id, int event);
}
