package com.example.soundwave.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "music_table")
public class Music {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    @ColumnInfo(name = "sample_rate")
    private String sampleRate;

    @ColumnInfo(name = "samples_16bit_pcm")
    private byte[] samples16BitPCM;

    public Music(String name, String sampleRate, byte[] samples16BitPCM) {
        this.name = name;
        this.sampleRate = sampleRate;
        this.samples16BitPCM = samples16BitPCM;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSamples16BitPCM(byte[] samples16BitPCM) {
        this.samples16BitPCM = samples16BitPCM;
    }

    public byte[] getSamples16BitPCM() {
        return samples16BitPCM;
    }
}
