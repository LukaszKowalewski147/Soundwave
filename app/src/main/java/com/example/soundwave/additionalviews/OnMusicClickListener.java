package com.example.soundwave.additionalviews;

import com.example.soundwave.components.Music;

public interface OnMusicClickListener {
    void onRenameClick(Music music);
    void onDeleteClick(Music music);
    void onDownloadClick(Music music);
    void playMusic(Music music, int position);
    void stopMusicPlaying(boolean anyMusic);
    boolean isMusicPlaying(int position);
    boolean isAnyMusicPlaying();
}