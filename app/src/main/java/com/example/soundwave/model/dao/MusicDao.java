package com.example.soundwave.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.soundwave.model.entity.Music;

import java.util.List;

@Dao
public interface MusicDao {

    @Insert
    void insert(Music music);

    @Update
    void update(Music music);

    @Delete
    void delete(Music music);

    @Query("DELETE FROM music_table")
    void deleteAllMusic();

    @Query("SELECT * FROM music_table ORDER BY id DESC")
    LiveData<List<Music>> getAllMusic();
}
