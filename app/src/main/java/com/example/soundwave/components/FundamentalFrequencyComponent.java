package com.example.soundwave.components;

import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.UnitsConverter;

public class FundamentalFrequencyComponent {

    private final int fundamentalFrequency;
    private final int fundamentalFrequencyBar;
    private final int masterVolume;
    private final String note;

    public FundamentalFrequencyComponent(int fundamentalFrequency, int masterVolume) {
        this.fundamentalFrequency = fundamentalFrequency;
        this.masterVolume = masterVolume;
        this.fundamentalFrequencyBar = assignBar();
        this.note = assignNote();
    }

    public static int calculateFrequencyOutOfScale(int position) {
        return 0;
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

    public String getNote() {
        return note;
    }

    private int assignBar() {
        return UnitsConverter.convertFrequencyToSeekBarProgress(fundamentalFrequency);
    }

    private String assignNote() {
        Scale[] scale = Scale.values();
        String note = scale[0].note;

        int currentDifference;
        int previousDifference = Math.abs(fundamentalFrequency - scale[0].frequency);

        for (int i = 1; i < Scale.values().length; ++i) {
            Scale scaleItem = scale[i];
            currentDifference = Math.abs(fundamentalFrequency - scaleItem.frequency);
            if (currentDifference > previousDifference)
                break;
            previousDifference = currentDifference;
            note = scaleItem.note;
        }
        return note;
    }
}
