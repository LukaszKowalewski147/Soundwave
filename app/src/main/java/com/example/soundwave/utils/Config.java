package com.example.soundwave.utils;

public enum Config {
    FREQUENCY_MIN(20),                      // in Hz
    FREQUENCY_MAX(20000),                   // in Hz
    FREQUENCY_DEFAULT(500),                 // in Hz
    FREQUENCY_PROGRESS_BAR_MAX(850),        // progress bar position
    FREQUENCY_PROGRESS_BAR_DEFAULT(425),    // progress bar position
    DURATION_MIN(1),                        // in seconds
    DURATION_MAX(60),                       // in seconds
    DURATION_DEFAULT(30),                   // in seconds
    PLAYBACK_REFRESH_RATE(20),              // in ms [20 ms = 50FPS]
    SEEKBAR_CHANGE_REFRESH_RATE(40),        // in ms [40 ms = 25FPS]
    TONES_NUMBER(2),
    ENVELOPE_ATTACK_DEFAULT(100),
    ENVELOPE_DECAY_DEFAULT(250),
    ENVELOPE_SUSTAIN_LEVEL_DEFAULT(40),
    ENVELOPE_SUSTAIN_DURATION_DEFAULT(1000),
    ENVELOPE_RELEASE_DEFAULT(200),
    ENVELOPE_MAX_PARAMETER_TIME(10000),
    OVERTONES_NUMBER(15);

    public final int value;

    Config(int value) {
        this.value = value;
    }
}