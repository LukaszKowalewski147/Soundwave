package com.example.soundwave.utils;

public enum PresetOvertones {
    // {15 overtones}
    NONE(new double[]{0.0d}),
    FLAT(new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}),
    PIANO(new double[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    ACOUSTIC_GUITAR(new double[]{66, 36, 2, 7, 20, 4, 2, 22, 10, 2, 4, 9, 13, 1, 1}),
    BASS_GUITAR(new double[]{15.0, 6.5, 12.5, 9.5, 8.0, -5.0, -6.0, -12.5, -15.0, -10.0, -3.0, -7.5, -6.5, -11.0, -18.0}),
    ELECTRIC_GUITAR(new double[]{66, 36, 2, 7, 20, 4, 2, 22, 10, 2, 4, 9, 13, 1, 1}),
    FLUTE(new double[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    TRUMPET(new double[]{65, 100, 60, 58, 40, 20, 42, 30, 21, 12, 22, 13, 16, 13, 12}),
    CUSTOM(new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d});

    public final double[] amplitudes;

    PresetOvertones(double[] amplitudes) {
        this.amplitudes = amplitudes;
    }
}
