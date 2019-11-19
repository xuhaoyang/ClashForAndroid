package com.github.kr328.clash;

import com.github.kr328.clash.model.ClashStatus;

interface IClashObserver {
    void onStatusChanged(in ClashStatus status);
}
