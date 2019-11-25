package com.github.kr328.clash.service;

import com.github.kr328.clash.core.event.Event;

interface IClashEventObserver {
    void onProcessEvent(in ProcessEvent event);
    void onProxyChangedEvent(in ProxyChangedEvent event);
    void onLogEvent(in LogEvent event);
    void onErrorEvent(in ErrorEvent event);
    void onTrafficEvent(in TrafficEvent event);
    void onProfileChanged(in ProfileChangedEvent event);
}
