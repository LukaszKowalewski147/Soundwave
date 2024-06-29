package com.example.soundwave.recyclerviews;

import com.example.soundwave.model.entity.Tone;

public interface OnToneClickListener {
    void onRenameClick(Tone tone);
    void onDeleteClick(Tone tone);
    void onEditClick(Tone tone);
    void onPlayClick(Tone tone);
}
