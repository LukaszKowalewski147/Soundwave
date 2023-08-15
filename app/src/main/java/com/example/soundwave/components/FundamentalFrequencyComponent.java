package com.example.soundwave.components;

import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.UnitsConverter;

public class FundamentalFrequencyComponent {

    private final int fundamentalFrequency;
    private final int fundamentalFrequencyBar;
    private final int masterVolume;
    private final int noteIndex;

    public FundamentalFrequencyComponent(int fundamentalFrequency, int masterVolume) {
        this.fundamentalFrequency = fundamentalFrequency;
        this.masterVolume = masterVolume;
        this.fundamentalFrequencyBar = assignBar();
        this.noteIndex = assignNoteIndex();
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

    public String getNoteName() {
        return Scale.values()[noteIndex].noteName;
    }

    private int assignBar() {
        return UnitsConverter.convertFrequencyToSeekBarProgress(fundamentalFrequency);
    }

    private int assignNoteIndex() {
        Scale[] scale = Scale.values();
        int index = 0;

        int currentDifference;
        int previousDifference = Math.abs(fundamentalFrequency - scale[0].noteFrequency);

        for (int i = 1; i < Scale.values().length; ++i) {
            Scale scaleItem = scale[i];
            currentDifference = Math.abs(fundamentalFrequency - scaleItem.noteFrequency);
            if (currentDifference > previousDifference)
                break;
            previousDifference = currentDifference;
            index = i;
        }
        return index;
    }
}
