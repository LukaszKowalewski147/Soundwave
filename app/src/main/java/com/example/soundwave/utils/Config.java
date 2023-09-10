package com.example.soundwave.utils;

public enum Config {
    FREQUENCY_MIN(20),                      // in Hz
    FREQUENCY_MAX(20000),                   // in Hz
    FREQUENCY_DEFAULT(500),                 // in Hz
    FREQUENCY_PROGRESS_BAR_MAX(850),        // progress bar position
    MASTER_VOLUME_DEFAULT(100),             // in %
    DURATION_DEFAULT(30),                   // in seconds
    PLAYBACK_REFRESH_RATE(20),              // in ms [20 ms = 50FPS]
    SEEKBAR_CHANGE_REFRESH_RATE(40),        // in ms [40 ms = 25FPS]
    ENVELOPE_PARAMETER_MIN_DURATION(0),
    ENVELOPE_PARAMETER_MAX_DURATION(10000),
    ENVELOPE_PARAMETER_MIN_LEVEL(0),
    ENVELOPE_PARAMETER_MAX_LEVEL(100),
    OVERTONES_NUMBER(15);

    public final int value;

    Config(int value) {
        this.value = value;
    }
}