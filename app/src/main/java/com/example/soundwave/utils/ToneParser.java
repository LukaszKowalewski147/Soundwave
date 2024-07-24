package com.example.soundwave.utils;

import com.example.soundwave.Overtone;
import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;

import java.util.ArrayList;

public class ToneParser {

    public Tone parseToneFromDb(com.example.soundwave.model.entity.Tone dbTone) {
        int id = dbTone.getId();
        String name = dbTone.getName();
        SampleRate sampleRate = parseSampleRate(dbTone);
        EnvelopeComponent ec = parseEnvelopeComponent(dbTone);
        FundamentalFrequencyComponent ffc = parseFundamentalFrequencyComponent(dbTone);
        OvertonesComponent oc = parseOvertonesComponent(dbTone);

        Tone tone = new ToneGenerator(sampleRate, ec.getTotalDurationInSeconds()).generateTone(ec, ffc, oc);
        tone.setId(id);
        tone.setName(name);

        return tone;
    }

    public Music parseMusicFromDb(com.example.soundwave.model.entity.Music dbMusic) {
        int id = dbMusic.getId();
        String name = dbMusic.getName();
        SampleRate sampleRate = parseSampleRate(dbMusic);
        byte[] samples = dbMusic.getSamples16BitPCM();

        Music music = new Music(sampleRate, samples);
        music.setId(id);
        music.setName(name);

        return music;
    }

    public com.example.soundwave.model.entity.Tone parseToneToDbEntity(Tone tone) {
        String toneName = tone.getName();
        int toneFrequency = tone.getFundamentalFrequency();
        int toneVolume = tone.getMasterVolume();
        String envelopeComponent = tone.getEnvelopeComponent().toString();
        String overtonesPreset = tone.getOvertonesPreset().toString();

        String overtonesDetails = "";

        ArrayList<Overtone> overtones = tone.getOvertones();
        if (overtones != null) {
            StringBuilder overtonesDetailsBuilder = new StringBuilder();
            for (Overtone overtone : overtones) {
                overtonesDetailsBuilder.append(overtone.toString());
            }
            overtonesDetails = overtonesDetailsBuilder.toString();
        }
        String overtonesComponent = overtonesPreset + "!" + overtonesDetails;
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());

        return new com.example.soundwave.model.entity.Tone(toneName, toneFrequency,
                toneVolume, envelopeComponent, overtonesComponent, sampleRate);
    }

    public com.example.soundwave.model.entity.Music parseMusicToDbEntity(Music music) {
        String musicName = music.getName();
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(music.getSampleRate());
        byte[] samples16BitPCM = music.getSamples16BitPCM();

        return new com.example.soundwave.model.entity.Music(musicName, sampleRate, samples16BitPCM);
    }


    private SampleRate parseSampleRate(com.example.soundwave.model.entity.Tone dbTone) {
        return UnitsConverter.convertStringToSampleRate(dbTone.getSampleRate());
    }

    private SampleRate parseSampleRate(com.example.soundwave.model.entity.Music dbMusic) {
        return UnitsConverter.convertStringToSampleRate(dbMusic.getSampleRate());
    }

    private EnvelopeComponent parseEnvelopeComponent(com.example.soundwave.model.entity.Tone dbTone) {
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

    private FundamentalFrequencyComponent parseFundamentalFrequencyComponent(com.example.soundwave.model.entity.Tone dbTone) {
        return new FundamentalFrequencyComponent(dbTone.getFundamentalFrequency(), dbTone.getVolume());
    }

    private OvertonesComponent parseOvertonesComponent(com.example.soundwave.model.entity.Tone dbTone) {
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
