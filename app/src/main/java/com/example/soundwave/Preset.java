package com.example.soundwave;

public enum Preset {
    // {15 overtones}
    FLAT(new int[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100}),
    PIANO(new int[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    ACOUSTIC_GUITAR(new int[]{66, 36, 2, 7, 20, 4, 2, 22, 10, 2, 4, 9, 13, 1, 1}),
    TRUMPET(new int[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    CUSTOM_TONE_1(new int[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100}),
    CUSTOM_TONE_2(new int[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100});

    final int[] amplitudes;

    Preset(int[] amplitudes) {
        this.amplitudes = amplitudes;
    }
}
