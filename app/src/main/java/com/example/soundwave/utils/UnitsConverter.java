package com.example.soundwave.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public static PresetOvertones convertPositionToPresetOvertones(int position) {
        switch (position) {
            case 0:
                return PresetOvertones.FLAT;
            case 1:
                return PresetOvertones.PIANO;
            case 2:
                return PresetOvertones.ACOUSTIC_GUITAR;
            case 3:
                return PresetOvertones.BASS_GUITAR;
            case 4:
                return PresetOvertones.ELECTRIC_GUITAR;
            case 5:
                return PresetOvertones.FLUTE;
            case 6:
                return PresetOvertones.TRUMPET;
            case 7:
                return PresetOvertones.CUSTOM;
        }
        return PresetOvertones.FLAT;
    }

    public static PresetEnvelope convertPositionToPresetEnvelope(int position) {
        switch (position) {
            case 0:
                return PresetEnvelope.FLAT;
            case 1:
                return PresetEnvelope.PIANO;
            case 2:
                return PresetEnvelope.GUITAR;
            case 3:
                return PresetEnvelope.FLUTE;
            case 4:
                return PresetEnvelope.TRUMPET;
            case 5:
                return PresetEnvelope.CUSTOM;
        }
        return PresetEnvelope.FLAT;
    }

    public static SampleRate convertPositionToSampleRate(int position) {
        switch (position) {
            case 0:
                return SampleRate.RATE_44_1_KHZ;
            case 1:
                return SampleRate.RATE_48_KHZ;
            case 2:
                return SampleRate.RATE_96_KHZ;
            case 3:
                return SampleRate.RATE_192_KHZ;
        }
        return SampleRate.RATE_44_1_KHZ;
    }

    public static int convertPresetEnvelopeToPosition(PresetEnvelope preset) {
        switch (preset) {
            case FLAT:
                return 0;
            case PIANO:
                return 1;
            case GUITAR:
                return 2;
            case FLUTE:
                return 3;
            case TRUMPET:
                return 4;
            case CUSTOM:
                return 5;
        }
        return 0;
    }

    public static int convertPresetOvertonesToPosition(PresetOvertones preset) {
        switch (preset) {
            case FLAT:
                return 0;
            case PIANO:
                return 1;
            case ACOUSTIC_GUITAR:
                return 2;
            case BASS_GUITAR:
                return 3;
            case ELECTRIC_GUITAR:
                return 4;
            case FLUTE:
                return 5;
            case TRUMPET:
                return 6;
            case CUSTOM:
                return 7;
        }
        return 0;
    }

    public static int convertSampleRateToPosition(SampleRate sampleRate) {
        switch (sampleRate) {
            case RATE_44_1_KHZ:
                return 0;
            case RATE_48_KHZ:
                return 1;
            case RATE_96_KHZ:
                return 2;
            case RATE_192_KHZ:
                return 3;
        }
        return 0;
    }

    public static String convertSampleRateToStringVisible(SampleRate sampleRate) {
        switch (sampleRate) {
            case RATE_44_1_KHZ:
                return "44.1kHz";
            case RATE_48_KHZ:
                return "48kHz";
            case RATE_96_KHZ:
                return "96kHz";
            case RATE_192_KHZ:
                return "192kHz";
        }
        return "-";
    }

    public static SampleRate convertStringToSampleRate(String sampleRate) {
        switch (sampleRate) {
            case "44.1kHz":
                return SampleRate.RATE_44_1_KHZ;
            case "48kHz":
                return SampleRate.RATE_48_KHZ;
            case "96kHz":
                return SampleRate.RATE_96_KHZ;
            case "192kHz":
                return SampleRate.RATE_192_KHZ;
        }
        return SampleRate.RATE_44_1_KHZ;
    }

    public static String convertPresetEnvelopeToString(PresetEnvelope preset) {
        switch (preset) {
            case FLAT:
                return "FLAT";
            case PIANO:
                return "PIANO";
            case GUITAR:
                return "GUITAR";
            case FLUTE:
                return "FLUTE";
            case TRUMPET:
                return "TRUMPET";
            case CUSTOM:
                return "CUSTOM";
        }
        return "-";
    }

    public static PresetEnvelope convertStringToPresetEnvelope(String preset) {
        switch (preset) {
            case "FLAT":
                return PresetEnvelope.FLAT;
            case "PIANO":
                return PresetEnvelope.PIANO;
            case "GUITAR":
                return PresetEnvelope.GUITAR;
            case "FLUTE":
                return PresetEnvelope.FLUTE;
            case "TRUMPET":
                return PresetEnvelope.TRUMPET;
            case "CUSTOM":
                return PresetEnvelope.CUSTOM;
        }
        return PresetEnvelope.CUSTOM;
    }

    public static String convertPresetOvertonesToString(PresetOvertones preset) {
        switch (preset) {
            case NONE:
                return "-";
            case FLAT:
                return "FLAT";
            case PIANO:
                return "PIANO";
            case ACOUSTIC_GUITAR:
                return "ACOUSTIC_GUITAR";
            case BASS_GUITAR:
                return "BASS_GUITAR";
            case ELECTRIC_GUITAR:
                return "ELECTRIC_GUITAR";
            case FLUTE:
                return "FLUTE";
            case TRUMPET:
                return "TRUMPET";
            case CUSTOM:
                return "CUSTOM";
        }
        return "-";
    }

    public static PresetOvertones convertStringToPresetOvertones(String preset) {
        switch (preset) {
            case "NONE":
                return PresetOvertones.NONE;
            case "FLAT":
                return PresetOvertones.FLAT;
            case "PIANO":
                return PresetOvertones.PIANO;
            case "ACOUSTIC_GUITAR":
                return PresetOvertones.ACOUSTIC_GUITAR;
            case "BASS_GUITAR":
                return PresetOvertones.BASS_GUITAR;
            case "ELECTRIC_GUITAR":
                return PresetOvertones.ELECTRIC_GUITAR;
            case "FLUTE":
                return PresetOvertones.FLUTE;
            case "TRUMPET":
                return PresetOvertones.TRUMPET;
            case "CUSTOM":
                return PresetOvertones.CUSTOM;
        }
        return PresetOvertones.CUSTOM;
    }

    public static double convertOvertoneDbSliderToHumanValue(int decibelValue) {
        double noPrecisionDb = (decibelValue / 10.0d) - 40;
        return BigDecimal.valueOf(noPrecisionDb)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static int convertOvertoneDbHumanValueToSliderProgress(double decibelValue) {
        return (int) (decibelValue + 40) * 10;
    }

    public static int convertFrequencyToNoteIndex(int frequency) {
        Scale[] scale = Scale.values();
        int index = 0;

        int currentDifference;
        int previousDifference = Math.abs(frequency - scale[0].noteFrequency);

        for (int i = 1; i < Scale.values().length; ++i) {
            Scale scaleItem = scale[i];
            currentDifference = Math.abs(frequency - scaleItem.noteFrequency);
            if (currentDifference > previousDifference)
                break;
            previousDifference = currentDifference;
            index = i;
        }

        return index;
    }

    public static String convertFrequencyToNote(int frequency) {
        int index = convertFrequencyToNoteIndex(frequency);
        return Scale.values()[index].noteName;
    }

    public static double convertMillisecondsToSeconds(int milliseconds) {
        return milliseconds / 1000.0d;
    }

    public static int convertSecondsToMilliseconds(double seconds) {
        return (int) Math.round(seconds * 1000);
    }
}