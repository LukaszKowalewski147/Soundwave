package com.example.soundwave.recyclerviews;

import android.content.Context;
import android.view.LayoutInflater;
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

    public ToneViewAdapter(Context context, List<Tone> tones) {
        this.context = context;
        this.tones = tones;
    }

    @NonNull
    @Override
    public ToneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToneViewHolder(LayoutInflater.from(context).inflate(R.layout.tone, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToneViewHolder holder, int position) {
        holder.toneName.setText(tones.get(position).getName());
        int frequency = tones.get(position).getFundamentalFrequency();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);
        holder.toneFrequency.setText(frequency + "Hz (" + scale + ")");
        holder.toneEnvelope.setText(tones.get(position).getEnvelope());
        holder.toneTimbre.setText(tones.get(position).getTimbre());
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
