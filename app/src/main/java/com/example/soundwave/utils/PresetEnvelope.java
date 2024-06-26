package com.example.soundwave.utils;

public enum PresetEnvelope {
    // PRESET_NAME {[0]-attack/ms, [1]-decay/ms, [2]-sustain level/%, [3]-sustain duration/ms, [4]-release/ms}
    FLAT(new int[]{100, 100, 50, 100, 100}),
    PIANO(new int[]{10, 200, 70, 500, 200}),
    GUITAR(new int[]{5, 0, 100, 100, 5000}),
    FLUTE(new int[]{200, 50, 90, 500, 200}),
    TRUMPET(new int[]{100, 50, 80, 500, 200}),
    CUSTOM(new int[]{500, 500, 50, 500, 500});

    public final int[] values;

    PresetEnvelope(int[] values) {
        this.values = values;
    }
}
