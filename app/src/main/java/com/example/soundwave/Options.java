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

    public enum ButtonLongPressState {
        PRESSED,
        RELEASED
    }

    public enum Operation {
        FREQUENCY_INCREMENT,
        FREQUENCY_DECREMENT,
        DURATION_INCREMENT,
        DURATION_DECREMENT
    }

    public static PlaybackMode playbackMode = PlaybackMode.STATIC;
    public static volatile PlaybackState playbackState = PlaybackState.OFF;
    public static volatile LooperState looperState = LooperState.OFF;
    public static volatile ButtonLongPressState buttonIncrementFrequencyState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonDecrementFrequencyState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonIncrementDurationState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonDecrementDurationState = ButtonLongPressState.RELEASED;
}
