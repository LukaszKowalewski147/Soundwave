package com.example.soundwave.model.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.soundwave.model.dao.ToneDao;
import com.example.soundwave.model.entity.Tone;

@Database(entities = {Tone.class}, version = 2)
public abstract class SoundwaveDatabase extends RoomDatabase {

    private static SoundwaveDatabase instance;

    public abstract ToneDao toneDao();
   // public abstract MusicDao musicDao();

    public static synchronized SoundwaveDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), SoundwaveDatabase.class, "soundwave_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
