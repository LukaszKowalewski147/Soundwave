package com.example.soundwave;

public class Options {

    public enum PlaybackMode {
        STATIC,
        STREAM
    }

    public enum PlaybackState {
        ON,
        OFF
    }

    public enum LooperState {
        ON,
        OFF
    }

    public static PlaybackMode playbackMode = PlaybackMode.STATIC;
    public static volatile PlaybackState playbackState = PlaybackState.OFF;
    public static volatile LooperState looperState = LooperState.OFF;
}
