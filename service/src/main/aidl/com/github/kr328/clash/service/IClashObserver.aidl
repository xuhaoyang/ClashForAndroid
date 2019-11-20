package com.github.kr328.clash.service;

import com.github.kr328.clash.core.ClashProcessStatus;

interface IClashObserver {
    void onStatusChanged(in ClashProcessStatus status);
}
