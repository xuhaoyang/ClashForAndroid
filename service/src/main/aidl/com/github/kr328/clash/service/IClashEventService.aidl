package com.github.kr328.clash.service;

import com.github.kr328.clash.service.IClashEventObserver;

interface IClashEventService {
    void registerEventObserver(String id, in IClashEventObserver observer, in int[] events);
    void unregisterEventObserver(String id);
}
