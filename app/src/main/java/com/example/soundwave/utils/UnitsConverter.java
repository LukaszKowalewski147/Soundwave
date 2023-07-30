package com.example.soundwave.utils;

public class UnitsConverter {

    private static final int frequencyOffset = 500;
    private static final int frequencyTargetCurvePower = 10;
    private static final int frequencyMinValueEqualizer = 19;

    /*
    Mathematical function used to present logarithmic nature of distinguishable sound frequency
    in human-friendly way: y=x^10 [x: 1 - 2.7] which gives results: y=1 for x=1; y=20589 for x=2.7.
    Maximum seek bar value has to be int and seek bar has to have enough precision.
    Multiplying range [1 - 2.7] by 500 gives seek bar enough precision - [500 - 1350].
    The offset of 500 points can be subtracted giving seek bar operating range of [0 - 850]
    */

    public static int convertSeekBarProgressToFrequency(int progress) {
        double input = progress + frequencyOffset;                      // adding offset (500) to reach [500 - 1350] range
        double targetCurveValue;
        int frequencyValue;

        input /= frequencyOffset;                                       // getting range [1 - 2.7] out of [500 - 1350]
        targetCurveValue = Math.pow(input, frequencyTargetCurvePower);  // applying target curve y=x^10
        frequencyValue = (int) Math.round(targetCurveValue);            // min = 1Hz for the lowest seek bar progress
        frequencyValue += frequencyMinValueEqualizer;                   // that's why adding equalizer (19) to reach 20Hz minimum

        if (frequencyValue > Config.FREQUENCY_MAX.value)                // crop excessive frequency value because
            frequencyValue = Config.FREQUENCY_MAX.value;                // max = 20608Hz for the highest seek bar progress

        return frequencyValue;
    }

    // Following function logic is the inversion of logic behind
    // converting seek bar progress to frequency value
    public static int convertFrequencyToSeekBarProgress(int frequency) {
        int input = frequency;
        int seekBarProgress;
        double tenthRoot;

        input -= frequencyMinValueEqualizer;
        tenthRoot = Math.pow(input, (double) 1 / frequencyTargetCurvePower);
        tenthRoot *= frequencyOffset;
        seekBarProgress = (int) Math.round(tenthRoot);

        return seekBarProgress - frequencyOffset;
    }
}
