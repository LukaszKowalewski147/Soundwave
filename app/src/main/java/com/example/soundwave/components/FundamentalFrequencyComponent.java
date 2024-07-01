package com.example.soundwave.components;

import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.UnitsConverter;

import java.io.Serializable;

public class FundamentalFrequencyComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int fundamentalFrequency;
    private final int fundamentalFrequencyBar;
    private final int masterVolume;
    private final int noteIndex;

    public FundamentalFrequencyComponent(int fundamentalFrequency, int masterVolume) {
        this.fundamentalFrequency = fundamentalFrequency;
        this.masterVolume = masterVolume;
        this.fundamentalFrequencyBar = assignBar();
        this.noteIndex = UnitsConverter.convertFrequencyToNoteIndex(fundamentalFrequency);
    }

    public static int getFrequencyOutOfNoteIndex(int noteIndex) {
        return Scale.values()[noteIndex].noteFrequency;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public int getFundamentalFrequencyBar() {
        return fundamentalFrequencyBar;
    }

    public int getMasterVolume() {
        return masterVolume;
    }

    public int getNoteIndex() {
        return noteIndex;
    }

    private int assignBar() {
        return UnitsConverter.convertFrequencyToSeekBarProgress(fundamentalFrequency);
    }
}
