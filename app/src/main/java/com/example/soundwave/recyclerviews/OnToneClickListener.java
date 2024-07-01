package com.example.soundwave.recyclerviews;

import com.example.soundwave.model.entity.Tone;

public interface OnToneClickListener {
    void onRenameClick(Tone tone);
    void onDeleteClick(Tone tone);
    void onEditClick(Tone tone);
    void onPlayStopClick(Tone tone);
    void onDownloadClick(Tone tone);
    boolean isPlaying(Tone tone);
}
