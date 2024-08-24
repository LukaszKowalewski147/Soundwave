package com.example.soundwave.view;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.soundwave.R;
import com.example.soundwave.components.ControlPanelComponent;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.sound.Overtone;
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.databinding.FragmentToneCreatorBinding;
import com.example.soundwave.databinding.OvertoneCreatorBinding;
import com.example.soundwave.databinding.ToneCreatorCustomEnvelopeLayoutBinding;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.OnFragmentExitListener;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.SeekBarUpdater;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneCreatorViewModel;

import java.util.Objects;

public class ToneCreatorFragment extends Fragment implements OnFragmentExitListener {
    private final String TAG = "ToneCreatorFragment";

    private ToneCreatorViewModel viewModel;
    private FragmentToneCreatorBinding binding;
    private ToneCreatorCustomEnvelopeLayoutBinding customEnvelopeBinding;
    private OvertoneCreatorBinding[] overtoneBindings;

    private boolean isCustomEnvelopeViewInflated = false;
    private View customEnvelopeView;

    private Tone editedTone;
    private boolean editorMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneCreatorBinding.inflate(inflater, container, false);
        initializeOvertoneBindings();

        viewModel = new ViewModelProvider(this).get(ToneCreatorViewModel.class);

        initializeDefaultLayout();
        initializeObservers();

        viewModel.getIsDataLoading().observe(getViewLifecycleOwner(), isDataLoading -> {
            if (!isDataLoading)
                initializeUIListeners();
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            editorMode = true;
            editedTone = (Tone) bundle.getSerializable("tone");
            boolean loadingSuccessful = viewModel.loadEditedTone(editedTone);

            if (!loadingSuccessful) {
                Log.e(TAG, "Load edited tone: loading unsuccessful");
                Toast.makeText(requireContext(), R.string.error_loading_tone, Toast.LENGTH_SHORT).show();
            }
            initializeToneEditorLayout();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        Options.lastOvertonePreset = PresetOvertones.FLAT;
        binding = null;
        customEnvelopeBinding = null;
        overtoneBindings = null;
        super.onDestroyView();
    }

    private void initializeOvertoneBindings() {
        overtoneBindings = new OvertoneCreatorBinding[Config.OVERTONES_NUMBER.value];
        overtoneBindings[0] = binding.toneCreatorOvertone0;
        overtoneBindings[1] = binding.toneCreatorOvertone1;
        overtoneBindings[2] = binding.toneCreatorOvertone2;
        overtoneBindings[3] = binding.toneCreatorOvertone3;
        overtoneBindings[4] = binding.toneCreatorOvertone4;
        overtoneBindings[5] = binding.toneCreatorOvertone5;
        overtoneBindings[6] = binding.toneCreatorOvertone6;
        overtoneBindings[7] = binding.toneCreatorOvertone7;
        overtoneBindings[8] = binding.toneCreatorOvertone8;
        overtoneBindings[9] = binding.toneCreatorOvertone9;
        overtoneBindings[10] = binding.toneCreatorOvertone10;
        overtoneBindings[11] = binding.toneCreatorOvertone11;
        overtoneBindings[12] = binding.toneCreatorOvertone12;
        overtoneBindings[13] = binding.toneCreatorOvertone13;
        overtoneBindings[14] = binding.toneCreatorOvertone14;
    }

    private void initializeDefaultLayout() {
        //  Spinners
        ArrayAdapter<CharSequence> sampleRatesAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        sampleRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorSampleRatesSpinner.setAdapter(sampleRatesAdapter);

        ArrayAdapter<CharSequence> envelopePresetAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.envelope_presets_array, android.R.layout.simple_spinner_item);
        envelopePresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorEnvelopePresetSpinner.setAdapter(envelopePresetAdapter);

        ArrayAdapter<CharSequence> overtonesPresetAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.overtones_presets_array, android.R.layout.simple_spinner_item);
        overtonesPresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorOvertonesPreset.setAdapter(overtonesPresetAdapter);

        //  Note picker
        Scale[] notes = Scale.values();
        int notesCount = notes.length;
        String[] noteNames = new String[notesCount];

        for (int i = 0; i < notesCount; ++i) {
            noteNames[i] = notes[i].noteName;
        }

        binding.toneCreatorNoteInput.setMinValue(0);
        binding.toneCreatorNoteInput.setMaxValue(notesCount - 1);
        binding.toneCreatorNoteInput.setDisplayedValues(noteNames);

        //  Bars
        binding.toneCreatorFundamentalFrequencyBar.setMax(Config.FREQUENCY_PROGRESS_BAR_MAX.value);

        //  Overtone indexes
        for (int i = 0; i < overtoneBindings.length; i++) {
            overtoneBindings[i].overtoneCreatorIndex.setText(viewModel.getIndexWithSuffix(i + 1));
        }

        //  Layout visibility
        binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
        binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);

        manageToneDetails(null);
    }

    private void initializeObservers() {
        viewModel.getSampleRate().observe(getViewLifecycleOwner(), sampleRate -> binding.toneCreatorSampleRatesSpinner.setSelection(UnitsConverter.convertSampleRateToPosition(sampleRate)));

        viewModel.getEnvelopeComponent().observe(getViewLifecycleOwner(), envelopeComponent -> {
            int selectedPresetPosition = binding.toneCreatorEnvelopePresetSpinner.getSelectedItemPosition();
            int actualPresetPosition = viewModel.getEnvelopePresetPosition();

            if (selectedPresetPosition != actualPresetPosition)
                binding.toneCreatorEnvelopePresetSpinner.setSelection(actualPresetPosition);

            if (envelopeComponent.getEnvelopePreset() == PresetEnvelope.CUSTOM) {
                inflateCustomEnvelopeLayout();

                int attackDurationMilliseconds = envelopeComponent.getAttackDurationMilliseconds();
                int decayDurationMilliseconds = envelopeComponent.getDecayDurationMilliseconds();
                int sustainLevelPercent = envelopeComponent.getSustainLevelPercent();
                int sustainDurationMilliseconds = envelopeComponent.getSustainDurationMilliseconds();
                int releaseDurationMilliseconds = envelopeComponent.getReleaseDurationMilliseconds();

                if (!customEnvelopeBinding.toneCreatorEnvelopeAttack.getText().toString().equals(String.valueOf(attackDurationMilliseconds)))
                    customEnvelopeBinding.toneCreatorEnvelopeAttack.setText(String.valueOf(attackDurationMilliseconds));
                if (!customEnvelopeBinding.toneCreatorEnvelopeDecay.getText().toString().equals(String.valueOf(decayDurationMilliseconds)))
                    customEnvelopeBinding.toneCreatorEnvelopeDecay.setText(String.valueOf(decayDurationMilliseconds));
                if (!customEnvelopeBinding.toneCreatorEnvelopeSustainLevel.getText().toString().equals(String.valueOf(sustainLevelPercent)))
                    customEnvelopeBinding.toneCreatorEnvelopeSustainLevel.setText(String.valueOf(sustainLevelPercent));
                if (!customEnvelopeBinding.toneCreatorEnvelopeSustainDuration.getText().toString().equals(String.valueOf(sustainDurationMilliseconds)))
                    customEnvelopeBinding.toneCreatorEnvelopeSustainDuration.setText(String.valueOf(sustainDurationMilliseconds));
                if (!customEnvelopeBinding.toneCreatorEnvelopeRelease.getText().toString().equals(String.valueOf(releaseDurationMilliseconds)))
                    customEnvelopeBinding.toneCreatorEnvelopeRelease.setText(String.valueOf(releaseDurationMilliseconds));
            }
        });

        viewModel.getToneDuration().observe(getViewLifecycleOwner(), duration -> {
            if (!binding.toneCreatorDurationInput.getText().toString().equals(String.valueOf(duration)))
                binding.toneCreatorDurationInput.setText(String.valueOf(duration));
        });

        viewModel.getFundamentalFrequencyComponent().observe(getViewLifecycleOwner(), fundamentalFrequencyComponent -> {
            int fundamentalFrequency = fundamentalFrequencyComponent.getFundamentalFrequency();
            int fundamentalFrequencyBar = fundamentalFrequencyComponent.getFundamentalFrequencyBar();
            int masterVolume = fundamentalFrequencyComponent.getMasterVolume();
            int noteIndex = fundamentalFrequencyComponent.getNoteIndex();

            if (!binding.toneCreatorFundamentalFrequencyInput.getText().toString().equals(String.valueOf(fundamentalFrequency)))
                binding.toneCreatorFundamentalFrequencyInput.setText(String.valueOf(fundamentalFrequency));

            binding.toneCreatorNoteInput.setValue(noteIndex);
            binding.toneCreatorFundamentalFrequencyBar.setProgress(fundamentalFrequencyBar);
            binding.toneCreatorMasterVolumeInput.setText(String.valueOf(masterVolume));
            binding.toneCreatorMasterVolumeBar.setProgress(masterVolume);

            manageMasterVolumeIcon(masterVolume);
        });

        viewModel.getControlPanelComponent().observe(getViewLifecycleOwner(), this::manageControlPanel);

        viewModel.getOvertones().observe(getViewLifecycleOwner(), overtones -> {
            for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
                updateOvertoneView(i, overtones[i]);
                manageOvertoneVolumeIcon(i, overtones[i].getAmplitude());
            }
            binding.toneCreatorOvertonesPreset.setSelection(viewModel.getOvertonesPresetPosition());
        });

        viewModel.getTone().observe(getViewLifecycleOwner(), this::manageToneDetails);
    }

    private void updateOvertoneView(int overtoneIndex, Overtone overtone) {
        double amplitude = overtone.getAmplitude();
        String overtoneFrequency = overtone.getFrequency() + getString(R.string.affix_Hz);
        String overtoneVolume = amplitude >= 0 ? "+" : "";
        overtoneVolume += amplitude + getString(R.string.affix_dB);

        overtoneBindings[overtoneIndex].overtoneCreatorActivator.setChecked(overtone.isActive());
        overtoneBindings[overtoneIndex].overtoneCreatorFrequency.setText(overtoneFrequency);
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeInput.setText(overtoneVolume);
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeBar.setProgress(UnitsConverter.convertOvertoneDbHumanValueToSliderProgress(amplitude));
    }

    private void initializeUIListeners() {
        binding.toneCreatorSampleRatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateSampleRate(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.toneCreatorEnvelopePresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateEnvelopePreset(position);

                if (Options.envelopePreset == PresetEnvelope.CUSTOM)
                    inflateCustomEnvelopeLayout();
                else
                    hideCustomEnvelopeLayout();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.toneCreatorDurationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateToneDuration(s.toString());
            }
        });

        binding.toneCreatorNoteInput.setOnValueChangedListener((picker, oldVal, newVal) -> viewModel.updateNoteName(newVal));

        binding.toneCreatorFundamentalFrequencyInput.addTextChangedListener(new TextWatcher() {
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

        binding.toneCreatorFundamentalFrequencyInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
        });

        binding.toneCreatorFundamentalFrequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        binding.toneCreatorFrequencyDecrementBtn.setOnClickListener(v -> viewModel.decrementOnceFundamentalFrequency());

        binding.toneCreatorFrequencyDecrementBtn.setOnLongClickListener(v -> {
            Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
            SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), binding.toneCreatorFundamentalFrequencyInput, Options.Operation.FREQUENCY_DECREMENT);
            Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
            seekBarUpdaterThread.start();
            return true;
        });

        binding.toneCreatorFrequencyDecrementBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
            return false;
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnClickListener(v -> viewModel.incrementOnceFundamentalFrequency());

        binding.toneCreatorFrequencyIncrementBtn.setOnLongClickListener(v -> {
            Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
            SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), binding.toneCreatorFundamentalFrequencyInput, Options.Operation.FREQUENCY_INCREMENT);
            Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
            seekBarUpdaterThread.start();
            return true;
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
            return false;
        });

        binding.toneCreatorMasterVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateMasterVolumeSeekBarPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.toneCreatorOvertonesActivator.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateOvertonesState(isChecked);
            if (isChecked) {
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(1000);

                binding.toneCreatorOvertonesLayout.startAnimation(fadeIn);
                binding.toneCreatorOvertonesPreset.startAnimation(fadeIn);

                binding.toneCreatorOvertonesLayout.setVisibility(View.VISIBLE);
                binding.toneCreatorOvertonesPreset.setVisibility(View.VISIBLE);
                return;
            }
            binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
            binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);
        });

        binding.toneCreatorOvertonesPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateOvertonesPreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        for (int i = 0; i < overtoneBindings.length; ++i) {
            int index = i;
            overtoneBindings[index].overtoneCreatorActivator.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateOvertoneState(index, isChecked));

            overtoneBindings[index].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        viewModel.updateOvertoneAmplitude(index, progress);
                        if (progress == 0)
                            overtoneBindings[index].overtoneCreatorActivator.setChecked(false);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        binding.toneCreatorGenerateToneBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.alert_dialog_tone_creator_generate_message);
            builder.setPositiveButton(R.string.alert_dialog_tone_creator_generate_positive, (dialog, id) -> {
                boolean generationSuccessful = viewModel.generateTone(editorMode);

                if (!generationSuccessful) {
                    Log.e(TAG, "Generate tone: generation unsuccessful");
                    Toast.makeText(requireContext(), R.string.error_generating_tone, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.alert_dialog_tone_creator_generate_negative, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        binding.toneCreatorPlayStopToneBtn.setOnClickListener(v -> viewModel.playStopTone());

        binding.toneCreatorSaveToneBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.alert_dialog_tone_creator_save_message);

            final EditText toneName = new EditText(getContext());
            toneName.setInputType(InputType.TYPE_CLASS_TEXT);

            if (editorMode)
                toneName.setText(editedTone.getName());

            builder.setView(toneName);

            builder.setPositiveButton(R.string.alert_dialog_tone_creator_save_positive, null);
            builder.setNegativeButton(R.string.alert_dialog_tone_creator_save_negative, null);

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String nameToSave = toneName.getText().toString().trim();
                if (!nameToSave.isEmpty()) {
                    viewModel.saveTone(nameToSave, editorMode);
                    dialog.dismiss();
                } else {
                    toneName.setError(getString(R.string.error_msg_empty_name));
                }
            }));

            dialog.show();
        });

        binding.toneCreatorResetToneBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.alert_dialog_tone_creator_reset_message);
            builder.setPositiveButton(R.string.alert_dialog_tone_creator_reset_positive, (dialog, id) -> takeResetAction());
            builder.setNegativeButton(R.string.alert_dialog_tone_creator_reset_negative, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void initializeToneEditorLayout() {
        TextView toneName = binding.toneCreatorToneName;

        toneName.setText(editedTone.getName());
        toneName.setVisibility(View.VISIBLE);
        toneName.setSelected(true);

        binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());

        if (Objects.requireNonNull(viewModel.getTone().getValue()).getOvertonesPreset() != PresetOvertones.NONE) {
            binding.toneCreatorOvertonesActivator.setChecked(true);
            binding.toneCreatorOvertonesLayout.setVisibility(View.VISIBLE);
            binding.toneCreatorOvertonesPreset.setVisibility(View.VISIBLE);

            Overtone[] overtones = Objects.requireNonNull(viewModel.getOvertones().getValue());
            for (int i = 0; i < overtones.length; ++i) {
                boolean isActive = overtones[i].isActive();
                overtoneBindings[i].overtoneCreatorActivator.setChecked(isActive);
            }
        }

        viewModel.setNoChange();
    }

    private void inflateCustomEnvelopeLayout() {
        if (!isCustomEnvelopeViewInflated) {
            customEnvelopeView = binding.toneCreatorCustomEnvelopeLayout.inflate();
            isCustomEnvelopeViewInflated = true;

            customEnvelopeBinding = ToneCreatorCustomEnvelopeLayoutBinding.bind(customEnvelopeView);

            EnvelopeComponent ec = Objects.requireNonNull(viewModel.getEnvelopeComponent().getValue());

            int attackDurationMilliseconds = ec.getAttackDurationMilliseconds();
            int decayDurationMilliseconds = ec.getDecayDurationMilliseconds();
            int sustainLevelPercent = ec.getSustainLevelPercent();
            int sustainDurationMilliseconds = ec.getSustainDurationMilliseconds();
            int releaseDurationMilliseconds = ec.getReleaseDurationMilliseconds();

            customEnvelopeBinding.toneCreatorEnvelopeAttack.setText(String.valueOf(attackDurationMilliseconds));
            customEnvelopeBinding.toneCreatorEnvelopeDecay.setText(String.valueOf(decayDurationMilliseconds));
            customEnvelopeBinding.toneCreatorEnvelopeSustainLevel.setText(String.valueOf(sustainLevelPercent));
            customEnvelopeBinding.toneCreatorEnvelopeSustainDuration.setText(String.valueOf(sustainDurationMilliseconds));
            customEnvelopeBinding.toneCreatorEnvelopeRelease.setText(String.valueOf(releaseDurationMilliseconds));

            setCustomEnvelopeListeners();
        }

        showCustomEnvelopeLayout();
    }

    private void showCustomEnvelopeLayout() {
        if (customEnvelopeView.getVisibility() != View.VISIBLE)
            customEnvelopeView.setVisibility(View.VISIBLE);
    }

    private void hideCustomEnvelopeLayout() {
        if (isCustomEnvelopeViewInflated) {
            if (customEnvelopeView.getVisibility() != View.GONE)
                customEnvelopeView.setVisibility(View.GONE);
        }
    }

    private void setCustomEnvelopeListeners() {
        customEnvelopeBinding.toneCreatorEnvelopeAttack.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.ATTACK_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
            }
        });

        customEnvelopeBinding.toneCreatorEnvelopeDecay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.DECAY_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
            }
        });

        customEnvelopeBinding.toneCreatorEnvelopeSustainLevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.SUSTAIN_LEVEL, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
            }
        });

        customEnvelopeBinding.toneCreatorEnvelopeSustainDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.SUSTAIN_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
            }
        });

        customEnvelopeBinding.toneCreatorEnvelopeRelease.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.RELEASE_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
            }
        });
    }

    private void takeResetAction() {
        MainActivity mainActivity = (MainActivity) requireActivity();

        if (editorMode)
            mainActivity.resetToneCreator(editedTone);
        else
            mainActivity.resetToneCreator();
    }

    private void manageControlPanel(ControlPanelComponent controlPanelComponent) {
        Drawable icon;
        ControlPanelComponent.ButtonState generateBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.GENERATE);
        ControlPanelComponent.ButtonState playStopBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.PLAY_STOP);
        ControlPanelComponent.ButtonState saveBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.SAVE);
        ControlPanelComponent.ButtonState resetBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.RESET);

        if (generateBtnState != null) {
            switch (generateBtnState) {
                case STANDARD:
                    binding.toneCreatorGenerateToneBtn.setEnabled(true);
                    binding.toneCreatorGenerateToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    break;
                case INACTIVE:
                    binding.toneCreatorGenerateToneBtn.setEnabled(false);
                    binding.toneCreatorGenerateToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
            }
        }

        if (playStopBtnState != null) {
            switch (playStopBtnState) {
                case STANDARD:
                    binding.toneCreatorPlayStopToneBtn.setEnabled(true);
                    binding.toneCreatorPlayStopToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    binding.toneCreatorPlayStopToneBtn.setText(R.string.play_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_note, null);
                    binding.toneCreatorPlayStopToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case INACTIVE:
                    binding.toneCreatorPlayStopToneBtn.setEnabled(false);
                    binding.toneCreatorPlayStopToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
                    binding.toneCreatorPlayStopToneBtn.setText(R.string.play_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_note, null);
                    binding.toneCreatorPlayStopToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case SECOND_FUNCTION:
                    binding.toneCreatorPlayStopToneBtn.setEnabled(true);
                    binding.toneCreatorPlayStopToneBtn.setBackgroundResource(R.drawable.background_btn_red);
                    binding.toneCreatorPlayStopToneBtn.setText(R.string.stop_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_stop, null);
                    binding.toneCreatorPlayStopToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }

        if (saveBtnState != null) {
            switch (saveBtnState) {
                case STANDARD:
                    binding.toneCreatorSaveToneBtn.setEnabled(true);
                    binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    binding.toneCreatorSaveToneBtn.setText(R.string.save_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_save, null);
                    binding.toneCreatorSaveToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case INACTIVE:
                    binding.toneCreatorSaveToneBtn.setEnabled(false);
                    binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
                    binding.toneCreatorSaveToneBtn.setText(R.string.save_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_save, null);
                    binding.toneCreatorSaveToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case DONE:
                    binding.toneCreatorSaveToneBtn.setEnabled(false);
                    binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_green);
                    binding.toneCreatorSaveToneBtn.setText(R.string.saved_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done, null);
                    binding.toneCreatorSaveToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }

        if (resetBtnState != null) {
            switch (resetBtnState) {
                case STANDARD:
                    binding.toneCreatorResetToneBtn.setEnabled(true);
                    binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    break;
                case INACTIVE:
                    binding.toneCreatorResetToneBtn.setEnabled(false);
                    binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
            }
        }
    }

    private void manageToneDetails(Tone tone) {
        String sampleRateString = "-";
        String frequencyString = "-";
        String envelopeString = "-";
        String timbreString = "-";

        if (tone != null) {
            String note = UnitsConverter.convertFrequencyToNote(tone.getFundamentalFrequency());

            sampleRateString = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());
            frequencyString = tone.getFundamentalFrequency() + "Hz (" + note + ")";
            envelopeString = UnitsConverter.convertPresetEnvelopeToString(tone.getEnvelopePreset());
            timbreString = UnitsConverter.convertPresetOvertonesToString(tone.getOvertonesPreset());
        }
        binding.toneCreatorToneDetailsSampleRate.setText(sampleRateString);
        binding.toneCreatorToneDetailsFundamentalFrequency.setText(frequencyString);
        binding.toneCreatorToneDetailsEnvelopePreset.setText(envelopeString);
        binding.toneCreatorToneDetailsTimbrePreset.setText(timbreString);
    }

    private void manageMasterVolumeIcon(int masterVolume) {
        if (masterVolume == 0)
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_mute);
        else if (masterVolume < binding.toneCreatorMasterVolumeBar.getMax() / 3)
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_low);
        else if (masterVolume < 1 + (binding.toneCreatorMasterVolumeBar.getMax() / 3) * 2)
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_normal);
        else
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_high);
    }

    private void manageOvertoneVolumeIcon(int overtoneIndex, double volume) {
        double maxVolume = overtoneBindings[overtoneIndex].overtoneCreatorVolumeBar.getMax() / 10.0d;
        volume += 40;

        if (volume < maxVolume / 3)
            overtoneBindings[overtoneIndex].overtoneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_low);
        else if (volume < (maxVolume / 3) * 2)
            overtoneBindings[overtoneIndex].overtoneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_normal);
        else
            overtoneBindings[overtoneIndex].overtoneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_high);
    }

    @Override
    public boolean onFragmentExit(int fragmentId) {
        if (!viewModel.getAnyChange())
            return true;

        checkIfExit(fragmentId);
        return false;
    }

    private void checkIfExit(int fragmentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setMessage(R.string.alert_dialog_tone_creator_exit_message);
        builder.setPositiveButton(R.string.alert_dialog_tone_creator_exit_positive, (dialog, which) -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.changeFragmentFromToneCreator(fragmentId);
        });
        builder.setNegativeButton(R.string.alert_dialog_tone_creator_exit_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}