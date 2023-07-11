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

    public static volatile PlaybackMode playbackMode = PlaybackMode.STATIC;
    public static volatile PlaybackState playbackState = PlaybackState.OFF;
    public static volatile LooperState looperState = LooperState.OFF;
    public static volatile Preset tone1Preset = Preset.FLAT;
    public static volatile Preset tone2Preset = Preset.FLAT;
    public static volatile ButtonLongPressState buttonIncrementFrequencyState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonDecrementFrequencyState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonIncrementDurationState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonDecrementDurationState = ButtonLongPressState.RELEASED;
    public static volatile int soundSampleRate = SampleRate.RATE_44_1_KHZ.sampleRate;
    public static volatile short soundDuration = (short) Config.DURATION_DEFAULT.value;

    public static Preset convertStringToPreset(String presetString) {
        switch (presetString) {
            case "Custom":
                return Preset.FLAT;
            case "Acoustic guitar":
                return Preset.ACOUSTIC_GUITAR;
        }
        return Preset.FLAT;
    }
}
