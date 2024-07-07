package com.example.soundwave.additionalviews;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.soundwave.R;
import com.example.soundwave.Tone;
import com.example.soundwave.viewmodel.ToneMixerViewModel;
import java.util.List;

public class SelectToneToMixDialogFragment extends DialogFragment {
    private final OnToneSelectedListener listener;
    private ToneMixerViewModel viewModel;

    public SelectToneToMixDialogFragment(OnToneSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_tone_to_mix, container, false);
        LinearLayout toneContainer = view.findViewById(R.id.tone_container);

        viewModel = new ViewModelProvider((FragmentActivity) requireContext()).get(ToneMixerViewModel.class);
        viewModel.getAllTones().observe(getViewLifecycleOwner(), new Observer<List<Tone>>() {
            @Override
            public void onChanged(List<Tone> tones) {
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
        View view = getLayoutInflater().inflate(R.layout.dialog_tone_to_mix, null);

        TextView toneNameTextView = view.findViewById(R.id.tone_name_text_view);
        toneNameTextView.setText(tone.getName());

        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToneSelected(tone);
                dismiss();
            }
        });

        return view;
    }
}
