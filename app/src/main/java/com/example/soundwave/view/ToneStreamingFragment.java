package com.example.soundwave.view;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.example.soundwave.R;
import com.example.soundwave.databinding.FragmentToneStreamingBinding;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.SeekBarUpdater;
import com.example.soundwave.viewmodel.ToneStreamingViewModel;

public class ToneStreamingFragment extends Fragment {
    private ToneStreamingViewModel viewModel;
    private FragmentToneStreamingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneStreamingBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ToneStreamingViewModel.class);

        initializeDefaultLayout();
        initializeObservers();
        initializeUIListeners();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void initializeDefaultLayout() {
        //  Play/stop button
        binding.toneStreamingPlayStopBtn.setTag(false); // TRUE - playing; FALSE - not playing

        //  Note picker
        Scale[] notes = Scale.values();
        int notesCount = notes.length;
        String[] noteNames = new String[notesCount];

        for (int i = 0; i < notesCount; ++i) {
            noteNames[i] = notes[i].noteName;
        }

        binding.toneStreamingNoteInput.setMinValue(0);
        binding.toneStreamingNoteInput.setMaxValue(notesCount - 1);
        binding.toneStreamingNoteInput.setDisplayedValues(noteNames);

        //  Bars
        binding.toneStreamingFundamentalFrequencyBar.setMax(Config.FREQUENCY_PROGRESS_BAR_MAX.value);
    }

    private void initializeObservers() {
        viewModel.getFundamentalFrequencyComponent().observe(getViewLifecycleOwner(), fundamentalFrequencyComponent -> {
            int fundamentalFrequency = fundamentalFrequencyComponent.getFundamentalFrequency();
            int fundamentalFrequencyBar = fundamentalFrequencyComponent.getFundamentalFrequencyBar();
            int noteIndex = fundamentalFrequencyComponent.getNoteIndex();

            if (!binding.toneStreamingFundamentalFrequencyInput.getText().toString().equals(String.valueOf(fundamentalFrequency)))
                binding.toneStreamingFundamentalFrequencyInput.setText(String.valueOf(fundamentalFrequency));

            binding.toneStreamingNoteInput.setValue(noteIndex);
            binding.toneStreamingFundamentalFrequencyBar.setProgress(fundamentalFrequencyBar);
        });
    }

    private void initializeUIListeners() {
        binding.toneStreamingNoteInput.setOnValueChangedListener((picker, oldVal, newVal) -> viewModel.updateNoteName(newVal));

        binding.toneStreamingFundamentalFrequencyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateFundamentalFrequency(s.toString());
            }
        });

        binding.toneStreamingFundamentalFrequencyInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                viewModel.validateFundamentalFrequencyInput(binding.toneStreamingFundamentalFrequencyInput.getText().toString());
        });

        binding.toneStreamingFundamentalFrequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateFundamentalFrequencySeekBarPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.toneStreamingFrequencyDecrementBtn.setOnClickListener(v -> viewModel.decrementOnceFundamentalFrequency());

        binding.toneStreamingFrequencyDecrementBtn.setOnLongClickListener(v -> {
            Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
            SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), binding.toneStreamingFundamentalFrequencyInput, Options.Operation.FREQUENCY_DECREMENT);
            Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
            seekBarUpdaterThread.start();
            return true;
        });

        binding.toneStreamingFrequencyDecrementBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                viewModel.validateFundamentalFrequencyInput(binding.toneStreamingFundamentalFrequencyInput.getText().toString());
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
            return false;
        });

        binding.toneStreamingFrequencyIncrementBtn.setOnClickListener(v -> viewModel.incrementOnceFundamentalFrequency());

        binding.toneStreamingFrequencyIncrementBtn.setOnLongClickListener(v -> {
            Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
            SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), binding.toneStreamingFundamentalFrequencyInput, Options.Operation.FREQUENCY_INCREMENT);
            Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
            seekBarUpdaterThread.start();
            return true;
        });

        binding.toneStreamingFrequencyIncrementBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                viewModel.validateFundamentalFrequencyInput(binding.toneStreamingFundamentalFrequencyInput.getText().toString());
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
            return false;
        });

        binding.toneStreamingPlayStopBtn.setOnClickListener(view -> {
            Object tag = view.getTag();

            if (Boolean.TRUE.equals(tag)) {     // TRUE - was playing; FALSE - was not playing
                viewModel.stopPlayback();
                setPlayStopButtonState(false);
            } else {
                viewModel.startPlayback();
                setPlayStopButtonState(true);
            }
        });
    }

    private void setPlayStopButtonState(boolean playingState) {
        ImageButton button = binding.toneStreamingPlayStopBtn;

        int iconId = playingState ? R.drawable.ic_stop_round : R.drawable.ic_play_round;
        Drawable buttonIcon = ContextCompat.getDrawable(requireContext(), iconId);

        button.setImageDrawable(buttonIcon);
        button.setTag(playingState);

        String buttonInfo = playingState ? getString(R.string.msg_stream_to_stop) : getString(R.string.msg_stream_to_play);
        binding.toneStreamingPlayStopInfo.setText(buttonInfo);
    }
}