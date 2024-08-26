package com.example.soundwave.utils;

import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.components.sound.Music;
import com.example.soundwave.components.sound.Overtone;
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.soundgenerator.ToneGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class DatabaseParser {

    public Tone parseToneFromDb(com.example.soundwave.model.entity.Tone dbTone) {
        int id = dbTone.getId();
        String name = dbTone.getName();
        double durationInSeconds = UnitsConverter.convertMillisecondsToSeconds(dbTone.getDuration());
        SampleRate sampleRate = parseSampleRate(dbTone);
        EnvelopeComponent ec = parseEnvelopeComponent(dbTone);
        FundamentalFrequencyComponent ffc = parseFundamentalFrequencyComponent(dbTone);
        OvertonesComponent oc = parseOvertonesComponent(dbTone);

        Tone tone = new ToneGenerator(sampleRate, durationInSeconds).generateTone(ec, ffc, oc);
        tone.setId(id);
        tone.setName(name);

        return tone;
    }

    public Music parseMusicFromDb(com.example.soundwave.model.entity.Music dbMusic) {
        int id = dbMusic.getId();
        String name = dbMusic.getName();
        SampleRate sampleRate = parseSampleRate(dbMusic);
        String pcmDataFilepath = dbMusic.getPcmData16BitFilepath();
        byte[] pcmData = read16BitPcmData(pcmDataFilepath);

        Music music = new Music(sampleRate, pcmData);
        music.setId(id);
        music.setName(name);

        return music;
    }

    public com.example.soundwave.model.entity.Tone parseToneToDbEntity(Tone tone) {
        String toneName = tone.getName();
        int toneFrequency = tone.getFundamentalFrequency();
        int toneVolume = tone.getMasterVolume();
        int toneDuration = tone.getDurationMilliseconds();
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

        return new com.example.soundwave.model.entity.Tone(
                toneName, toneFrequency, toneVolume, toneDuration,
                envelopeComponent, overtonesComponent, sampleRate);
    }

    public com.example.soundwave.model.entity.Music parseMusicToDbEntity(Music music) {
        String musicName = music.getName();
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(music.getSampleRate());
        byte[] samples16BitPcm = music.getPcmData();
        long unixTime = System.currentTimeMillis();
        String pcmDataFileName = "m-" + musicName + "-" + unixTime;
        String pcmData16BitFilepath = save16BitPcmData(samples16BitPcm, pcmDataFileName);

        return new com.example.soundwave.model.entity.Music(
                musicName, sampleRate, pcmData16BitFilepath);
    }

    public com.example.soundwave.model.entity.Music parseMusicToDbEntityForDeletion(Music music) {
        com.example.soundwave.model.entity.Music musicToDelete =
                new com.example.soundwave.model.entity.Music("", "", "");

        musicToDelete.setId(music.getId());

        return musicToDelete;
    }

    private SampleRate parseSampleRate(com.example.soundwave.model.entity.Tone dbTone) {
        return UnitsConverter.convertStringToSampleRate(dbTone.getSampleRate());
    }

    private SampleRate parseSampleRate(com.example.soundwave.model.entity.Music dbMusic) {
        return UnitsConverter.convertStringToSampleRate(dbMusic.getSampleRate());
    }

    private EnvelopeComponent parseEnvelopeComponent(com.example.soundwave.model.entity.Tone dbTone) {
        String dbEnvelopeComponent = dbTone.getEnvelopeComponent();

        if (dbEnvelopeComponent == null || dbEnvelopeComponent.trim().isEmpty())
            return null;

        EnvelopeComponent envelopeComponent = null;
        String[] fields = dbEnvelopeComponent.split(",");

        if (fields.length == 6) {
            PresetEnvelope preset = UnitsConverter.convertStringToPresetEnvelope(fields[0].trim());
            int attackDurationMilliseconds = Integer.parseInt(fields[1].trim());
            int decayDurationMilliseconds = Integer.parseInt(fields[2].trim());
            int sustainLevelPercent = Integer.parseInt(fields[3].trim());
            int sustainDurationMilliseconds = Integer.parseInt(fields[4].trim());
            int releaseDurationMilliseconds = Integer.parseInt(fields[5].trim());

            envelopeComponent = new EnvelopeComponent(
                    preset, attackDurationMilliseconds,
                    decayDurationMilliseconds, sustainLevelPercent,
                    sustainDurationMilliseconds, releaseDurationMilliseconds);
        }
        return envelopeComponent;
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

    private String save16BitPcmData(byte[] pcmData, String filename) {
        File audioFile = new File(Options.filepathToSavePcmData, filename);

        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            fos.write(pcmData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return audioFile.getAbsolutePath();
    }

    private byte[] read16BitPcmData(String absoluteFilePath) {
        File audioFile = new File(absoluteFilePath);

        try {
            return Files.readAllBytes(audioFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
