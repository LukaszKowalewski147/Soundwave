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
/*
    private final double[] samples;

    public Tone(int fundamentalFrequency, double volume, double[] samples) {
        this.fundamentalFrequency = fundamentalFrequency;
        this.volume = volume;
        this.samples = samples;
    }
*/
    public Tone(int fundamentalFrequency, double volume) {
        this.fundamentalFrequency = fundamentalFrequency;
        this.volume = volume;
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
/*
    public double[] getSamples() {
        return samples;
    }*/
}
