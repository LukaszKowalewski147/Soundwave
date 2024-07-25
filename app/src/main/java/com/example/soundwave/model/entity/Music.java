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

    @ColumnInfo(name = "samples_16bit_pcm_filepath")
    private String samples16BitPcmFilepath;

    public Music(String name, String sampleRate, String samples16BitPcmFilepath) {
        this.name = name;
        this.sampleRate = sampleRate;
        this.samples16BitPcmFilepath = samples16BitPcmFilepath;
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

    public void setSamples16BitPcmFilepath(String samples16BitPcmFilepath) {
        this.samples16BitPcmFilepath = samples16BitPcmFilepath;
    }

    public String getSamples16BitPcmFilepath() {
        return samples16BitPcmFilepath;
    }
}
