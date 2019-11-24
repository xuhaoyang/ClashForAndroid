package com.github.kr328.clash.service;

import com.github.kr328.clash.core.model.ClashProcessStatus;

interface IClashObserver {
    void onProxyChanged();
    void onNewLogItem();
    void onProcessStatusChanged(in ClashProcessStatus status);
}
