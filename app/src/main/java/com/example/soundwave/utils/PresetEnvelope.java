package com.example.soundwave.utils;

public enum PresetEnvelope {
    // PRESET_NAME {[0]-attack/ms, [1]-decay/ms, [2]-sustain level/%, [3]-sustain duration/ms, [4]-release/ms}
    FLAT(new int[]{50, 0, 100, 1000, 100}),
    PIANO(new int[]{500, 500, 50, 500, 500}),
    GUITAR(new int[]{500, 500, 50, 500, 500}),
    FLUTE(new int[]{500, 500, 50, 500, 500}),
    TRUMPET(new int[]{500, 500, 50, 500, 500}),
    CUSTOM(new int[]{500, 500, 50, 500, 500});

    public final int[] values;

    PresetEnvelope(int[] values) {
        this.values = values;
    }
}
