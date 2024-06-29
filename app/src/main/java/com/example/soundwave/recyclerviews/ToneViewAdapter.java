package com.example.soundwave.recyclerviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundwave.R;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.utils.UnitsConverter;

import java.util.List;

public class ToneViewAdapter extends RecyclerView.Adapter<ToneViewHolder> {

    private Context context;
    private List<Tone> tones;
    private OnToneClickListener listener;

    public ToneViewAdapter(Context context, List<Tone> tones, OnToneClickListener listener) {
        this.context = context;
        this.tones = tones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToneViewHolder(LayoutInflater.from(context).inflate(R.layout.tone, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToneViewHolder holder, int position) {
        setMoreInfoVisibility(holder, false);
        Tone dbTone = tones.get(position);
        ToneParser parser = new ToneParser(dbTone);
        com.example.soundwave.Tone tone = parser.parseToneFromDb();

        int frequency = tone.getFundamentalFrequency();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);
        String envelopePreset = tone.getEnvelopePreset().toString();
        String overtonesPreset = tone.getOvertonesPreset().toString();
        int volume = tone.getMasterVolume();
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());

        holder.toneName.setText(dbTone.getName());
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
                listener.onRenameClick(dbTone);
            }
        });

        holder.toneDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               listener.onDeleteClick(dbTone);
            }
        });

        holder.toneMoreInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.toneVolumeHeader.getVisibility() == View.GONE)
                    setMoreInfoVisibility(holder, true);
                else
                    setMoreInfoVisibility(holder, false);
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
