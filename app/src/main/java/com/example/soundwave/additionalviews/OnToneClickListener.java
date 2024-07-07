package com.example.soundwave.additionalviews;

import com.example.soundwave.Tone;

public interface OnToneClickListener {
    void onRenameClick(Tone tone);
    void onDeleteClick(Tone tone);
    void onDownloadClick(Tone tone);
    void playTone(Tone tone, int position);
    void stopTonePlaying(boolean anyTone);
    boolean isTonePlaying(int position);
    boolean isAnyTonePlaying();

}
