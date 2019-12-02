package com.github.kr328.clash.service;

interface IClashSettingService {
    // Set
    void setIPv6Enabled(boolean enabled);
    void setBypassPrivateNetwork(boolean enabled);
    void setAccessControl(int mode, in String[] applications);

    // Get
    boolean isIPv6Enabled();
    boolean isBypassPrivateNetwork();
    String[] getAccessControlApps();
    int getAccessControlMode();
}
