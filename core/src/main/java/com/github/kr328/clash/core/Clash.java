package com.github.kr328.clash.core;

public final class Clash {
    public static native void init(String home);
    public static native void loadDefault();
    public static native void loadProfileFromPath(String path) throws ClashException;

    static {
        System.loadLibrary("clash");
    }
}
