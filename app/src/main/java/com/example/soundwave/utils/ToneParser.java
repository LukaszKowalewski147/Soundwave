package com.example.soundwave.utils;

import com.example.soundwave.Overtone;
import com.example.soundwave.Tone;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;

import java.util.ArrayList;

public class ToneParser {

    private final com.example.soundwave.model.entity.Tone dbTone;

    public ToneParser(com.example.soundwave.model.entity.Tone dbTone) {
        this.dbTone = dbTone;
    }

    public Tone parseToneFromDb() {
        SampleRate sampleRate = parseSampleRate();
        EnvelopeComponent ec = parseEnvelopeComponent();
        FundamentalFrequencyComponent ffc = parseFundamentalFrequencyComponent();
        OvertonesComponent oc = parseOvertonesComponent();

        return new ToneGenerator(sampleRate, ec, ffc, oc).generateTone();
    }

    private SampleRate parseSampleRate(){
        return UnitsConverter.convertStringToSampleRate(dbTone.getSampleRate());
    }

    private EnvelopeComponent parseEnvelopeComponent() {
        String dbEC = dbTone.getEnvelopeComponent();

        if (dbEC == null || dbEC.trim().isEmpty())
            return null;

        EnvelopeComponent eC = null;
        String[] fields = dbEC.split(",");

        if (fields.length == 6) {
            PresetEnvelope preset = UnitsConverter.convertStringToPresetEnvelope(fields[0].trim());
            int attackDuration = Integer.parseInt(fields[1].trim());
            int decayDuration = Integer.parseInt(fields[2].trim());
            int sustainLevel = Integer.parseInt(fields[3].trim());
            int sustainDuration = Integer.parseInt(fields[4].trim());
            int releaseDuration = Integer.parseInt(fields[5].trim());

            eC = new EnvelopeComponent(preset, attackDuration, decayDuration, sustainLevel, sustainDuration, releaseDuration);
        }
        return eC;
    }

    private FundamentalFrequencyComponent parseFundamentalFrequencyComponent() {
        return new FundamentalFrequencyComponent(dbTone.getFundamentalFrequency(), dbTone.getVolume());
    }

    private OvertonesComponent parseOvertonesComponent() {
        String overtones = dbTone.getOvertonesComponent();

        if (overtones == null || overtones.trim().isEmpty())
            return null;

        String[] entries = overtones.split("!");
        PresetOvertones preset = UnitsConverter.convertStringToPresetOvertones(entries[0].trim());

        ArrayList<Overtone> overtonesList = new ArrayList<>();
        if (entries.length > 1) {
            String[] overtoneEntries = entries[1].split(";");

            for (String entry : overtoneEntries) {
                if (!entry.trim().isEmpty()) {
                    String[] fields = entry.split(",");
                    if (fields.length == 4) {
                        int index = Integer.parseInt(fields[0].trim());
                        int frequency = Integer.parseInt(fields[1].trim());
                        double amplitude = Double.parseDouble(fields[2].trim());
                        boolean active = Boolean.parseBoolean(fields[3].trim());
                        overtonesList.add(new Overtone(index, frequency, amplitude, active));
                    }
                }
            }
        }
        return new OvertonesComponent(overtonesList, preset);
    }
}
