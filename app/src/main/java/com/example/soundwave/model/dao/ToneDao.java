package com.example.soundwave.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.soundwave.model.entity.Tone;

import java.util.List;

@Dao
public interface ToneDao {

    @Insert
    void insert(Tone tone);

    @Update
    void update(Tone tone);

    @Delete
    void delete(Tone tone);

    @Query("DELETE FROM tone_table")
    void deleteAllTones();

    @Query("SELECT * FROM tone_table ORDER BY id DESC")
    LiveData<List<Tone>> getAllTones();
}
