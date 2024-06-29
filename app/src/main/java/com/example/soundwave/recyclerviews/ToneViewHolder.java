package com.example.soundwave.recyclerviews;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundwave.R;

public class ToneViewHolder extends RecyclerView.ViewHolder {

    TextView toneName;
    TextView toneFrequency;
    TextView toneEnvelope;
    TextView toneTimbre;
    ImageButton toneDeleteBtn;
    AppCompatButton toneRenameBtn;
    AppCompatButton toneMoreInfoBtn;


    public ToneViewHolder(@NonNull View itemView) {
        super(itemView);
        toneName = itemView.findViewById(R.id.tone_name);
        toneFrequency = itemView.findViewById(R.id.tone_details_frequency);
        toneEnvelope = itemView.findViewById(R.id.tone_details_envelope_preset);
        toneTimbre = itemView.findViewById(R.id.tone_details_timbre_preset);
        toneDeleteBtn = itemView.findViewById(R.id.tone_delete_btn);
        toneRenameBtn = itemView.findViewById(R.id.tone_rename_btn);
        toneMoreInfoBtn = itemView.findViewById(R.id.tone_more_info_btn);
    }
}
