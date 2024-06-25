package com.example.soundwave.utils;

public enum PresetOvertones {
    // {15 overtones}
    FLAT(new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}),
    PIANO(new double[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    ACOUSTIC_GUITAR(new double[]{66, 36, 2, 7, 20, 4, 2, 22, 10, 2, 4, 9, 13, 1, 1}),
    BASS_GUITAR(new double[]{66, 36, 2, 7, 20, 4, 2, 22, 10, 2, 4, 9, 13, 1, 1}),
    ELECTRIC_GUITAR(new double[]{66, 36, 2, 7, 20, 4, 2, 22, 10, 2, 4, 9, 13, 1, 1}),
    FLUTE(new double[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    TRUMPET(new double[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    CUSTOM(new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d});

    public final double[] amplitudes;

    PresetOvertones(double[] amplitudes) {
        this.amplitudes = amplitudes;
    }
}
