package com.example.soundwave.viewadapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundwave.R;

public class MusicViewHolder extends RecyclerView.ViewHolder {

    ConstraintLayout parentLayout;
    TextView musicName;
    TextView musicSampleRateHeader;
    TextView musicSampleRate;
    TextView musicDuration;
    ImageButton musicDeleteBtn;
    ImageButton musicPlayStopBtn;
    ImageButton musicDownloadBtn;
    AppCompatButton musicRenameBtn;

    public MusicViewHolder(@NonNull View itemView) {
        super(itemView);

        parentLayout = itemView.findViewById(R.id.parent_music_layout);
        musicName = itemView.findViewById(R.id.music_name);
        musicSampleRateHeader = itemView.findViewById(R.id.music_details_sample_rate_header);
        musicSampleRate = itemView.findViewById(R.id.music_details_sample_rate);
        musicDuration = itemView.findViewById(R.id.music_details_duration);
        musicDeleteBtn = itemView.findViewById(R.id.music_delete_btn);
        musicPlayStopBtn = itemView.findViewById(R.id.music_play_stop_btn);
        musicDownloadBtn = itemView.findViewById(R.id.music_download_btn);
        musicRenameBtn = itemView.findViewById(R.id.music_rename_btn);
    }
}
