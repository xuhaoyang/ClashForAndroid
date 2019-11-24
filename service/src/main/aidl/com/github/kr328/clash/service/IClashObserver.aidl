package com.github.kr328.clash.service;

import com.github.kr328.clash.core.ClashProcessStatus;

interface IClashObserver {
    void onProxyChanged();
    void onNewLog();
    void onProcessStatusChanged(in ClashProcessStatus status);
}
