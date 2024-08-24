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
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.view.MainActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ToneViewAdapter extends RecyclerView.Adapter<ToneViewHolder> {

    private final Context context;
    private List<Tone> tones;
    private final OnToneClickListener listener;
    private final Map<Integer, Boolean> expandedPositions;

    public ToneViewAdapter(Context context, List<Tone> tones, OnToneClickListener listener) {
        this.context = context;
        this.tones = tones;
        this.listener = listener;
        this.expandedPositions = new HashMap<>();
    }

    @NonNull
    @Override
    public ToneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToneViewHolder(LayoutInflater.from(context).inflate(R.layout.tone, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToneViewHolder holder, @SuppressLint("RecyclerView") int position) {
        boolean isExpanded = Boolean.TRUE.equals(expandedPositions.getOrDefault(position, false));
        setMoreInfoVisibility(holder, isExpanded);

        Tone tone = tones.get(position);

        int frequency = tone.getFundamentalFrequency();
        int volume = tone.getMasterVolume();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);

        String frequencyDisplay = frequency + context.getString(R.string.affix_Hz) + " (" + scale + ")";
        String envelopePreset = tone.getEnvelopePreset().toString();
        String timbre = tone.getOvertonesPreset().toString();
        String volumeDisplay = volume + context.getString(R.string.affix_percent);
        String overtonesNumber = String.valueOf(tone.getOvertonesComponent().getActiveOvertonesNumber());
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());
        String duration = String.format(Locale.US, "%.3fs", tone.getDurationSeconds());

        holder.toneName.setSelected(true);
        holder.toneName.setText(tone.getName());
        holder.toneFrequency.setText(frequencyDisplay);
        holder.toneEnvelope.setText(envelopePreset);
        holder.toneTimbre.setText(timbre);
        holder.toneVolume.setText(volumeDisplay);
        holder.toneOvertonesNumber.setText(overtonesNumber);
        holder.toneSampleRate.setText(sampleRate);
        holder.toneDuration.setText(duration);

        holder.toneRenameBtn.setOnClickListener(v -> listener.onRenameClick(tone));

        holder.toneDeleteBtn.setOnClickListener(v -> {
            stopPlaybackIfOccurs(holder, position);
            listener.onDeleteClick(tone);
        });

        boolean isTonePlaying = listener.isTonePlaying(position);
        int color = isTonePlaying ? ContextCompat.getColor(context, R.color.delete_bin) : ContextCompat.getColor(context, R.color.white);

        holder.parentLayout.setBackgroundResource(isTonePlaying ? R.drawable.background_shadow_active : R.drawable.background_shadow_item);
        holder.tonePlayStopBtn.setImageResource(isTonePlaying ? R.drawable.ic_stop : R.drawable.ic_play_tone);
        holder.tonePlayStopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        holder.tonePlayStopBtn.setOnClickListener(v -> {
            if (listener.isTonePlaying(position)) {
                listener.stopTonePlaying(false);
                setNotPlayingLayout(holder);
                return;
            }
            if (listener.isAnyTonePlaying()) {
                listener.stopTonePlaying(true);
            }
            listener.playTone(tone, position);
            setPlayingLayout(holder);
        });

        holder.toneEditBtn.setOnClickListener(v -> {
            stopPlaybackIfOccurs(holder, position);
            if (context instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.openToneCreatorInEditionMode(tone);
            }
        });

        holder.toneDownloadBtn.setOnClickListener(v -> listener.onDownloadClick(tone));

        holder.toneMoreInfoBtn.setOnClickListener(v -> {
            boolean expanded = Boolean.TRUE.equals(expandedPositions.getOrDefault(position, false));
            expandedPositions.put(position, !expanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return tones.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setToneItems(List<Tone> newTones) {
        if (newTones != null) {
            tones = newTones;
            notifyDataSetChanged();
        }
    }

    private void stopPlaybackIfOccurs(ToneViewHolder holder, int position) {
        if (listener.isAnyTonePlaying()) {
            if (listener.isTonePlaying(position)) {
                listener.stopTonePlaying(false);
                setNotPlayingLayout(holder);
            } else
                listener.stopTonePlaying(true);
        }
    }

    private void setPlayingLayout(ToneViewHolder holder) {
        int color = ContextCompat.getColor(context, R.color.delete_bin);

        holder.parentLayout.setBackgroundResource(R.drawable.background_shadow_active);
        holder.tonePlayStopBtn.setImageResource(R.drawable.ic_stop);
        holder.tonePlayStopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setNotPlayingLayout(ToneViewHolder holder) {
        int color = ContextCompat.getColor(context, R.color.white);

        holder.parentLayout.setBackgroundResource(R.drawable.background_shadow_item);
        holder.tonePlayStopBtn.setImageResource(R.drawable.ic_play_tone);
        holder.tonePlayStopBtn.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setMoreInfoVisibility(ToneViewHolder holder, boolean visible) {
        String buttonText = context.getString(R.string.more_info_btn);
        int visibility = View.GONE;

        if (visible) {
            buttonText = context.getString(R.string.less_info_btn);
            visibility = View.VISIBLE;
        }

        holder.toneMoreInfoBtn.setText(buttonText);
        holder.toneVolumeHeader.setVisibility(visibility);
        holder.toneVolume.setVisibility(visibility);
        holder.toneOvertonesNumberHeader.setVisibility(visibility);
        holder.toneOvertonesNumber.setVisibility(visibility);
        holder.toneSampleRateHeader.setVisibility(visibility);
        holder.toneSampleRate.setVisibility(visibility);
    }
}