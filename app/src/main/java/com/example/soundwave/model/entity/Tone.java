package com.example.soundwave.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tone_table")
public class Tone {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "fundamental_frequency")
    private final int fundamentalFrequency;

    private final double volume;

    private final float[] samples;

    public Tone(int fundamentalFrequency, double volume, float[] samples) {
        this.fundamentalFrequency = fundamentalFrequency;
        this.volume = volume;
        this.samples = samples;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public double getVolume() {
        return volume;
    }

    public float[] getSamples() {
        return samples;
    }
}
