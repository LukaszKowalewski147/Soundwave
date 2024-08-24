package com.example.soundwave.additionalviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.soundwave.R;
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneMixerViewModel;

import java.util.Locale;

public class SelectToneToMixDialogFragment extends DialogFragment {
    private final OnToneSelectedListener listener;

    public SelectToneToMixDialogFragment(OnToneSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_tone_to_mix, container, false);
        LinearLayout toneContainer = view.findViewById(R.id.tone_container);
        TextView emptyMsg = view.findViewById(R.id.empty_tones_msg);

        ToneMixerViewModel viewModel = new ViewModelProvider((FragmentActivity) requireContext()).get(ToneMixerViewModel.class);

        viewModel.getAllTones().observe(getViewLifecycleOwner(), tones -> {
            if (tones.isEmpty())
                emptyMsg.setVisibility(View.VISIBLE);
            else {
                toneContainer.removeAllViews();
                for (Tone tone : tones) {
                    View toneView = createToneView(tone);
                    toneContainer.addView(toneView);
                }
            }
        });

        return view;
    }

    private View createToneView(Tone tone) {
        View toneToMix = getLayoutInflater().inflate(R.layout.dialog_tone_to_mix, null);

        TextView toneName = toneToMix.findViewById(R.id.tone_to_mix_tone_name);
        TextView toneFrequency = toneToMix.findViewById(R.id.tone_to_mix_frequency);
        TextView toneVolume = toneToMix.findViewById(R.id.tone_to_mix_volume);
        TextView toneSampleRate = toneToMix.findViewById(R.id.tone_to_mix_sample_rate);
        TextView toneDuration = toneToMix.findViewById(R.id.tone_to_mix_duration);

        int frequency = tone.getFundamentalFrequency();

        String name = tone.getName();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);
        String frequencyDisplay = frequency + getString(R.string.affix_Hz) + " (" + scale + ")";
        String volume = tone.getMasterVolume() + getString(R.string.affix_percent);
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());
        String duration = String.format(Locale.US, "%.3fs", tone.getDurationSeconds());

        toneName.setSelected(true);
        toneName.setText(name);
        toneFrequency.setText(frequencyDisplay);
        toneVolume.setText(volume);
        toneSampleRate.setText(sampleRate);
        toneDuration.setText(duration);

        toneToMix.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToneSelected(tone);
                dismiss();
            }
        });

        return toneToMix;
    }
}
