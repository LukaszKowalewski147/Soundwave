package com.example.soundwave.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.databinding.OvertoneCreatorBinding;
import com.example.soundwave.model.entity.Overtone;
import com.example.soundwave.utils.Config;
import com.example.soundwave.R;
import com.example.soundwave.utils.Options;
import com.example.soundwave.databinding.FragmentToneCreatorBinding;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneCreatorViewModel;

public class ToneCreatorFragment extends Fragment {

    private ToneCreatorViewModel viewModel;
    private FragmentToneCreatorBinding binding;
    private OvertoneCreatorBinding[] overtoneBindings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneCreatorBinding.inflate(inflater, container, false);
        initializeOvertoneBindings();
        viewModel = new ViewModelProvider(this).get(ToneCreatorViewModel.class);
        initializeDefaultLayout();
        initializeObservers();
        initializeUIListeners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        overtoneBindings = null;
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
        ArrayAdapter<CharSequence> sampleRatesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        sampleRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorSampleRatesSpinner.setAdapter(sampleRatesAdapter);

        ArrayAdapter<CharSequence> envelopePresetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.envelope_presets_array, android.R.layout.simple_spinner_item);
        envelopePresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorEnvelopePresetSpinner.setAdapter(envelopePresetAdapter);

        ArrayAdapter<CharSequence> overtonesPresetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.overtones_presets_array, android.R.layout.simple_spinner_item);
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

        //  Control panel buttons
        binding.toneCreatorPlayToneBtn.setEnabled(false);
        binding.toneCreatorPlayToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);

        binding.toneCreatorSaveToneBtn.setEnabled(false);
        binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);

        binding.toneCreatorResetToneBtn.setEnabled(false);
        binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);

        //  Layout visibility
        binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
        binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);
    }

    private void initializeObservers() {
        viewModel.getSampleRate().observe(getViewLifecycleOwner(), new Observer<SampleRate>() {
            @Override
            public void onChanged(SampleRate sampleRate) {
                    binding.toneCreatorSampleRatesSpinner.setSelection(UnitsConverter.convertSampleRateToPosition(sampleRate));
            }
        });

        viewModel.getEnvelopeComponent().observe(getViewLifecycleOwner(), new Observer<EnvelopeComponent>() {
            @Override
            public void onChanged(EnvelopeComponent envelopeComponent) {
                int attackDuration = envelopeComponent.getAttackDuration();
                int decayDuration = envelopeComponent.getDecayDuration();
                int sustainLevel = envelopeComponent.getSustainLevel();
                int sustainDuration = envelopeComponent.getSustainDuration();
                int releaseDuration = envelopeComponent.getReleaseDuration();

                if (!binding.toneCreatorEnvelopeAttack.getText().toString().equals(String.valueOf(attackDuration)))
                    binding.toneCreatorEnvelopeAttack.setText(String.valueOf(attackDuration));
                if (!binding.toneCreatorEnvelopeDecay.getText().toString().equals(String.valueOf(decayDuration)))
                    binding.toneCreatorEnvelopeDecay.setText(String.valueOf(decayDuration));
                if (!binding.toneCreatorEnvelopeSustainLevel.getText().toString().equals(String.valueOf(sustainLevel)))
                    binding.toneCreatorEnvelopeSustainLevel.setText(String.valueOf(sustainLevel));
                if (!binding.toneCreatorEnvelopeSustainDuration.getText().toString().equals(String.valueOf(sustainDuration)))
                    binding.toneCreatorEnvelopeSustainDuration.setText(String.valueOf(sustainDuration));
                if (!binding.toneCreatorEnvelopeRelease.getText().toString().equals(String.valueOf(releaseDuration)))
                    binding.toneCreatorEnvelopeRelease.setText(String.valueOf(releaseDuration));
            }
        });

        viewModel.getFundamentalFrequencyComponent().observe(getViewLifecycleOwner(), new Observer<FundamentalFrequencyComponent>() {
            @Override
            public void onChanged(FundamentalFrequencyComponent fundamentalFrequencyComponent) {
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
            }
        });

        viewModel.getOvertones().observe(getViewLifecycleOwner(), new Observer<Overtone[]>() {
            @Override
            public void onChanged(Overtone[] overtones) {
                for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
                    updateOvertoneView(i, overtones[i]);
                }
                binding.toneCreatorOvertonesPreset.setSelection(viewModel.getOvertonesPresetPosition());
            }
        });

        viewModel.getAnyChange().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    binding.toneCreatorResetToneBtn.setEnabled(true);
                    binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    return;
                }
                binding.toneCreatorResetToneBtn.setEnabled(false);
                binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
            }
        });
    }

    private void updateOvertoneView(int overtoneIndex, Overtone overtone) {
        overtoneBindings[overtoneIndex].overtoneCreatorActivator.setChecked(overtone.isActive());
        overtoneBindings[overtoneIndex].overtoneCreatorFrequency.setText(String.valueOf(overtone.getFrequency()) + "Hz");
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeInput.setText(String.valueOf(overtone.getAmplitude()));
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeBar.setProgress(overtone.getAmplitude());
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.toneCreatorEnvelopeAttack.addTextChangedListener(new TextWatcher() {
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

        binding.toneCreatorEnvelopeDecay.addTextChangedListener(new TextWatcher() {
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

        binding.toneCreatorEnvelopeSustainLevel.addTextChangedListener(new TextWatcher() {
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

        binding.toneCreatorEnvelopeSustainDuration.addTextChangedListener(new TextWatcher() {
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

        binding.toneCreatorEnvelopeRelease.addTextChangedListener(new TextWatcher() {
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

        binding.toneCreatorNoteInput.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                viewModel.updateNoteName(newVal);
            }
        });

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

        binding.toneCreatorFundamentalFrequencyInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
            }
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

        binding.toneCreatorFrequencyDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.decrementOnceFundamentalFrequency();
            }
        });

        binding.toneCreatorFrequencyDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewModel.decrementConstantlyFundamentalFrequency();
                return true;
            }
        });

        binding.toneCreatorFrequencyDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.incrementOnceFundamentalFrequency();
            }
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewModel.incrementConstantlyFundamentalFrequency();
                return true;
            }
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
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

        binding.toneCreatorOvertonesActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertonesState(isChecked);
                if (isChecked) {
                    binding.toneCreatorOvertonesLayout.setVisibility(View.VISIBLE);
                    binding.toneCreatorOvertonesPreset.setVisibility(View.VISIBLE);
                    return;
                }
                binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
                binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);
            }
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
            overtoneBindings[index].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    viewModel.updateOvertoneState(index, isChecked);
                }
            });

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

        binding.toneCreatorGenerateToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.generateTone();
            }
        });

        binding.toneCreatorPlayToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.playTone();
            }
        });

        binding.toneCreatorSaveToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.saveTone();
            }
        });

        binding.toneCreatorResetToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_dialog_tone_creator_reset_message);
                builder.setPositiveButton(R.string.alert_dialog_tone_creator_reset_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takeResetAction();
                    }
                });
                builder.setNegativeButton(R.string.alert_dialog_tone_creator_reset_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void takeResetAction() {
        binding.toneCreatorOvertonesActivator.setChecked(false);
        viewModel.resetTone();
    }
}