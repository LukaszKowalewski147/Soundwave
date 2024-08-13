package com.example.soundwave.utils;

public enum PresetEnvelope {
    // PRESET_NAME {[0]-attack/ms, [1]-decay/ms, [2]-sustain level/%, [3]-sustain duration/ms, [4]-release/ms}
    FLAT(new int[]{0, 0, 100, 10000, 0}),
    PIANO(new int[]{50, 0, 100, 500, 40000}),
    GUITAR(new int[]{5, 0, 100, 250, 30000}),
    FLUTE(new int[]{200, 700, 80, 10000, 250}),
    TRUMPET(new int[]{300, 800, 80, 10000, 350}),
    CUSTOM(new int[]{500, 500, 50, 500, 500});

    public final int[] values;

    PresetEnvelope(int[] values) {
        this.values = values;
    }
}
