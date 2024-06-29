package com.example.soundwave.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tone_table")
public class Tone {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private final String name;

    @ColumnInfo(name = "fundamental_frequency")
    private final int fundamentalFrequency;

    private final String envelope;

    private final String timbre;

    private final double volume;

    @ColumnInfo(name = "overtones_data")
    private final String overtonesData;

    public Tone(String name, int fundamentalFrequency, String envelope, String timbre, double volume, String overtonesData) {
        this.name = name;
        this.fundamentalFrequency = fundamentalFrequency;
        this.envelope = envelope;
        this.timbre = timbre;
        this.volume = volume;
        this.overtonesData = overtonesData;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public String getEnvelope() {
        return envelope;
    }

    public String getTimbre() {
        return timbre;
    }

    public double getVolume() {
        return volume;
    }

    public String getOvertonesData() {
        return overtonesData;
    }
}
