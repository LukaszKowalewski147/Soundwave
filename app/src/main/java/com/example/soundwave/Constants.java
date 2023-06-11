package com.example.soundwave;

public enum Constants {
    FREQ_MIN (20),
    FREQ_MAX (20000),
    FREQUENCY_START(10000),
    DURATION_MIN (1),
    DURATION_MAX (60),
    DURATION_START (30);

    final int value;

    Constants(int value) {
        this.value = value;
    }
}