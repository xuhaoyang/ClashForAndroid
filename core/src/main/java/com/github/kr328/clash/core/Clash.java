package com.github.kr328.clash.core;

public final class Clash {
    public static native void init(String clashLibrary, String home);
    public static native void loadDefault();
    public static native void loadProfileFromPath(String path) throws ClashException;
    public static native void startTun(int fd, int mtu) throws ClashException;
    public static native void stopTun() throws ClashException;

    static {
        System.loadLibrary("clash");
        System.loadLibrary("bridge");
    }
}
