package com.example.soundwave;

public enum Constants {
    FREQ_MIN (20),              // in Hz
    FREQ_MAX (20000),           // in Hz
    FREQ_SLIDER_MIN (0),        // position
    FREQ_SLIDER_MAX (850),      // position
    FREQ_SLIDER_START (425),    // position
    DURATION_MIN (1),           // in s
    DURATION_MAX (60),          // in s
    DURATION_START (30);        // in s

    final int value;

    Constants(int value) {
        this.value = value;
    }
}