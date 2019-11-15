package com.github.kr328.clash.core;

public final class Clash {
    public static native void loadDefault();

    static {
        System.loadLibrary("clash");
    }
}
