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
    TextView toneVolumeHeader;
    TextView toneVolume;
    TextView toneOvertonesNumberHeader;
    TextView toneOvertonesNumber;
    TextView toneSampleRateHeader;
    TextView toneSampleRate;
    TextView toneOtherInfoHeader;
    TextView toneOtherInfo;
    ImageButton toneDeleteBtn;
    ImageButton tonePlayStopBtn;
    AppCompatButton toneRenameBtn;
    AppCompatButton toneMoreInfoBtn;


    public ToneViewHolder(@NonNull View itemView) {
        super(itemView);
        toneName = itemView.findViewById(R.id.tone_name);
        toneFrequency = itemView.findViewById(R.id.tone_details_frequency);
        toneEnvelope = itemView.findViewById(R.id.tone_details_envelope_preset);
        toneTimbre = itemView.findViewById(R.id.tone_details_timbre_preset);
        toneVolumeHeader = itemView.findViewById(R.id.tone_details_volume_header);
        toneVolume = itemView.findViewById(R.id.tone_details_volume);
        toneOvertonesNumberHeader = itemView.findViewById(R.id.tone_details_overtones_number_header);
        toneOvertonesNumber = itemView.findViewById(R.id.tone_details_overtones_number);
        toneSampleRateHeader = itemView.findViewById(R.id.tone_details_sample_rate_header);
        toneSampleRate = itemView.findViewById(R.id.tone_details_sample_rate);
        toneOtherInfoHeader = itemView.findViewById(R.id.tone_details_other_header);
        toneOtherInfo = itemView.findViewById(R.id.tone_details_other);
        toneDeleteBtn = itemView.findViewById(R.id.tone_delete_btn);
        tonePlayStopBtn = itemView.findViewById(R.id.tone_play_stop_btn);
        toneRenameBtn = itemView.findViewById(R.id.tone_rename_btn);
        toneMoreInfoBtn = itemView.findViewById(R.id.tone_more_info_btn);
    }
}
