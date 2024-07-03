package com.example.soundwave.recyclerviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundwave.MainActivity;
import com.example.soundwave.R;
import com.example.soundwave.Tone;
import com.example.soundwave.utils.UnitsConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToneViewAdapter extends RecyclerView.Adapter<ToneViewHolder> {

    private Context context;
    private List<Tone> tones;
    private OnToneClickListener listener;
    private Map<Integer, Boolean> expandedPositions;

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
        boolean isExpanded = expandedPositions.getOrDefault(position, false);
        setMoreInfoVisibility(holder, isExpanded);

        com.example.soundwave.Tone tone = tones.get(position);

        int frequency = tone.getFundamentalFrequency();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);
        String envelopePreset = tone.getEnvelopePreset().toString();
        String overtonesPreset = tone.getOvertonesPreset().toString();
        int volume = tone.getMasterVolume();
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());

        holder.toneName.setSelected(true);
        holder.toneName.setText(tone.getName());
        holder.toneFrequency.setText(frequency + "Hz (" + scale + ")");
        holder.toneEnvelope.setText(envelopePreset);
        holder.toneTimbre.setText(overtonesPreset);
        holder.toneVolume.setText(volume + "%");
        holder.toneOvertonesNumber.setText("14");
        holder.toneSampleRate.setText(sampleRate);
        holder.toneOtherInfo.setText("comming soon");

        holder.toneRenameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRenameClick(tone);
            }
        });

        holder.toneDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaybackIfOccurs(position);
                listener.onDeleteClick(tone);
            }
        });

        boolean isTonePlaying = listener.isTonePlaying(position);

        holder.parentLayout.setBackgroundResource(isTonePlaying ? R.drawable.background_shadow_active : R.drawable.background_shadow_item);
        holder.tonePlayStopBtn.setImageResource(isTonePlaying ? R.drawable.ic_stop : R.drawable.ic_play_tone);
        holder.tonePlayStopBtn.setColorFilter(ContextCompat.getColor(context, (isTonePlaying ? R.color.delete_bin : R.color.white)), PorterDuff.Mode.SRC_IN);

        holder.tonePlayStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.isTonePlaying(position)) {
                    listener.stopTonePlaying(false);

                    holder.parentLayout.setBackgroundResource(R.drawable.background_shadow_item);
                    holder.tonePlayStopBtn.setImageResource(R.drawable.ic_play_tone);
                    holder.tonePlayStopBtn.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
                    return;
                }
                if (listener.isAnyTonePlaying()) {
                    listener.stopTonePlaying(true);
                }
                listener.playTone(tone, position);

                holder.parentLayout.setBackgroundResource(R.drawable.background_shadow_active );
                holder.tonePlayStopBtn.setImageResource(R.drawable.ic_stop);
                holder.tonePlayStopBtn.setColorFilter(ContextCompat.getColor(context, R.color.delete_bin), PorterDuff.Mode.SRC_IN);
            }
        });

        holder.toneEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaybackIfOccurs(position);
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.openToneCreatorInEditionMode(tone);
                }
            }
        });

        holder.toneDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownloadClick(tone);
            }
        });

        holder.toneMoreInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean expanded = expandedPositions.getOrDefault(position, false);
                expandedPositions.put(position, !expanded);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tones.size();
    }

    public void setToneItems(List<Tone> newTones) {
        if (newTones != null) {
            tones = newTones;
            notifyDataSetChanged();
        }
    }

    private void stopPlaybackIfOccurs(int position) {
        if (listener.isAnyTonePlaying()) {
            if (listener.isTonePlaying(position))
                listener.stopTonePlaying(false);
            else
                listener.stopTonePlaying(true);
        }
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
        holder.toneOtherInfoHeader.setVisibility(visibility);
        holder.toneOtherInfo.setVisibility(visibility);
    }
}
