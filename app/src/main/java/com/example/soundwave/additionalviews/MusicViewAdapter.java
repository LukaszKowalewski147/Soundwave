package com.example.soundwave.additionalviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundwave.R;
import com.example.soundwave.components.Music;
import com.example.soundwave.utils.UnitsConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MusicViewAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private final Context context;
    private List<Music> musicList;
    private final OnMusicClickListener listener;
    private final Map<Integer, Boolean> expandedPositions;

    public MusicViewAdapter(Context context, List<Music> musicList, OnMusicClickListener listener) {
        this.context = context;
        this.musicList = musicList;
        this.listener = listener;
        this.expandedPositions = new HashMap<>();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(context).inflate(R.layout.music, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") int position) {
        boolean isExpanded = Boolean.TRUE.equals(expandedPositions.getOrDefault(position, false));
        setMoreInfoVisibility(holder, isExpanded);

        Music music = musicList.get(position);

        String name = music.getName();
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(music.getSampleRate());
        String duration = String.format(Locale.US, "%.3fs", music.getDurationInSeconds());

        holder.musicName.setSelected(true);
        holder.musicName.setText(name);
        holder.musicSampleRate.setText(sampleRate);
        holder.musicOtherInfo.setText("comming soon");
        holder.musicDuration.setText(duration);

        holder.musicRenameBtn.setOnClickListener(v -> listener.onRenameClick(music));

        holder.musicDeleteBtn.setOnClickListener(v -> {
            stopPlaybackIfOccurs(holder, position);
            listener.onDeleteClick(music);
        });

        boolean isMusicPlaying = listener.isMusicPlaying(position);
        int color = isMusicPlaying ? ContextCompat.getColor(context, R.color.delete_bin) : ContextCompat.getColor(context, R.color.white);

        holder.parentLayout.setBackgroundResource(isMusicPlaying ? R.drawable.background_shadow_active : R.drawable.background_shadow_item);
        holder.musicPlayStopBtn.setImageResource(isMusicPlaying ? R.drawable.ic_stop : R.drawable.ic_play_tone);
        holder.musicPlayStopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        holder.musicPlayStopBtn.setOnClickListener(v -> {
            if (listener.isMusicPlaying(position)) {
                listener.stopMusicPlaying(false);
                setNotPlayingLayout(holder);
                return;
            }
            if (listener.isAnyMusicPlaying()) {
                listener.stopMusicPlaying(true);
            }
            listener.playMusic(music, position);
            setPlayingLayout(holder);
        });

        holder.musicDownloadBtn.setOnClickListener(v -> listener.onDownloadClick(music));

        holder.musicMoreInfoBtn.setOnClickListener(v -> {
            boolean expanded = Boolean.TRUE.equals(expandedPositions.getOrDefault(position, false));
            expandedPositions.put(position, !expanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMusicItems(List<Music> newMusicList) {
        if (newMusicList != null) {
            musicList = newMusicList;
            notifyDataSetChanged();
        }
    }

    private void stopPlaybackIfOccurs(MusicViewHolder holder, int position) {
        if (listener.isAnyMusicPlaying()) {
            if (listener.isMusicPlaying(position)) {
                listener.stopMusicPlaying(false);
                setNotPlayingLayout(holder);
            } else
                listener.stopMusicPlaying(true);
        }
    }

    private void setPlayingLayout(MusicViewHolder holder) {
        int color = ContextCompat.getColor(context, R.color.delete_bin);

        holder.parentLayout.setBackgroundResource(R.drawable.background_shadow_active);
        holder.musicPlayStopBtn.setImageResource(R.drawable.ic_stop);
        holder.musicPlayStopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setNotPlayingLayout(MusicViewHolder holder) {
        int color = ContextCompat.getColor(context, R.color.white);

        holder.parentLayout.setBackgroundResource(R.drawable.background_shadow_item);
        holder.musicPlayStopBtn.setImageResource(R.drawable.ic_play_tone);
        holder.musicPlayStopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setMoreInfoVisibility(MusicViewHolder holder, boolean visible) {
        String buttonText = context.getString(R.string.more_info_btn);
        int visibility = View.GONE;

        if (visible) {
            buttonText = context.getString(R.string.less_info_btn);
            visibility = View.VISIBLE;
        }

        holder.musicMoreInfoBtn.setText(buttonText);
        holder.musicOtherInfoHeader.setVisibility(visibility);
        holder.musicOtherInfo.setVisibility(visibility);
    }
}
