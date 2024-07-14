package com.example.soundwave.utils;

public class Options {

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
        FREQUENCY_DECREMENT
    }

    public static volatile PlaybackState playbackState = PlaybackState.OFF;
    public static volatile LooperState looperState = LooperState.OFF;
    public static volatile PresetEnvelope envelopePreset = PresetEnvelope.FLAT;
    public static volatile PresetOvertones overtonePreset = PresetOvertones.NONE;
    public static volatile PresetOvertones lastOvertonePreset = PresetOvertones.FLAT;
    public static volatile ButtonLongPressState buttonIncrementFrequencyState = ButtonLongPressState.RELEASED;
    public static volatile ButtonLongPressState buttonDecrementFrequencyState = ButtonLongPressState.RELEASED;
    public static volatile String filepathToDownload = "";
    public static volatile float displayDensity;
    public static volatile int trackPaddingStart = 0;
}
