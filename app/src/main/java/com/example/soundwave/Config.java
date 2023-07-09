package com.example.soundwave;

public enum Config {
    FREQUENCY_MIN(20),                      // in Hz
    FREQUENCY_MAX(20000),                   // in Hz
    FREQUENCY_PROGRESS_BAR_MAX(850),        // progress bar position
    FREQUENCY_PROGRESS_BAR_DEFAULT(425),    // progress bar position
    DURATION_MIN(1),                        // in seconds
    DURATION_MAX(60),                       // in seconds
    DURATION_DEFAULT(30),                   // in seconds
    PLAYBACK_REFRESH_RATE(20),              // in ms [20 ms = 50FPS]
    SEEKBAR_CHANGE_REFRESH_RATE(40),        // in ms [40 ms = 25FPS]
    TONES_NUMBER(2),
    OVERTONES_NUMBER(15);

    final int value;

    Config(int value) {
        this.value = value;
    }
    }