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

    @ColumnInfo(name = "pcm_data_16bit_filepath")
    private String pcmData16BitFilepath;

    public Music(String name, String sampleRate, String pcmData16BitFilepath) {
        this.name = name;
        this.sampleRate = sampleRate;
        this.pcmData16BitFilepath = pcmData16BitFilepath;
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

    public void setPcmData16BitFilepath(String pcmData16BitFilepath) {
        this.pcmData16BitFilepath = pcmData16BitFilepath;
    }

    public String getPcmData16BitFilepath() {
        return pcmData16BitFilepath;
    }
}
