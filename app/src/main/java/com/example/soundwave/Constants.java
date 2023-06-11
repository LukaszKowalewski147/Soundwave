package com.example.soundwave;

public enum Constants {
    FREQ_MIN (20),
    FREQ_MAX (20000),
    FREQ_SLIDER_MIN (0),
    FREQ_SLIDER_MAX (850),
    FREQ_SLIDER_START (425),
    DURATION_MIN (1),
    DURATION_MAX (60),
    DURATION_START (30);

    final int value;

    Constants(int value) {
        this.value = value;
    }
}