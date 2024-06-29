package com.example.soundwave.recyclerviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soundwave.R;
import com.example.soundwave.model.entity.Tone;
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
        Tone tone = tones.get(position);

        int frequency = tone.getFundamentalFrequency();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);

        holder.toneName.setText(tone.getName());
        holder.toneFrequency.setText(frequency + "Hz (" + scale + ")");
        holder.toneEnvelope.setText(tone.getEnvelope());
        holder.toneTimbre.setText(tone.getTimbre());

        holder.toneName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRenameClick(tone);
            }
        });

        holder.toneDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               listener.onDeleteClick(tone);
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
}
