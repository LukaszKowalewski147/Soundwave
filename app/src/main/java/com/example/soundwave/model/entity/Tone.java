package com.example.soundwave.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tone_table")
public class Tone {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    @ColumnInfo(name = "fundamental_frequency")
    private int fundamentalFrequency;

    private int volume;

    private int duration;

    @ColumnInfo(name = "envelope_component")
    private String envelopeComponent;

    @ColumnInfo(name = "overtones_component")
    private String overtonesComponent;

    @ColumnInfo(name = "sample_rate")
    private String sampleRate;

    public Tone(String name, int fundamentalFrequency, int volume, int duration, String envelopeComponent, String overtonesComponent, String sampleRate) {
        this.name = name;
        this.fundamentalFrequency = fundamentalFrequency;
        this.volume = volume;
        this.duration = duration;
        this.envelopeComponent = envelopeComponent;
        this.overtonesComponent = overtonesComponent;
        this.sampleRate = sampleRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public void setFundamentalFrequency(int fundamentalFrequency) {
        this.fundamentalFrequency = fundamentalFrequency;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getEnvelopeComponent() {
        return envelopeComponent;
    }

    public void setEnvelopeComponent(String envelopeComponent) {
        this.envelopeComponent = envelopeComponent;
    }

    public String getOvertonesComponent() {
        return overtonesComponent;
    }

    public void setOvertonesComponent(String overtonesComponent) {
        this.overtonesComponent = overtonesComponent;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }
}
