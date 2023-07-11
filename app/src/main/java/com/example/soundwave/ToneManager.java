package com.example.soundwave;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ToneManager {

    private final Context context;
    private final View toneView;
    private final int index;

    private OvertoneManager[] overtoneManagers;
    private EditText fundamentalFrequencyInput;
    private SeekBar fundamentalFrequencyBar;
    private ImageButton frequencyDecrementBtn;
    private ImageButton frequencyIncrementBtn;
    private SeekBar masterAmplitudeBar;
    private TextView masterAmplitudeInput;

    private CheckBox overtonesActivator;
    private Spinner overtonesPreset;
    private LinearLayout overtonesLayout;

    private int fundamentalFrequencyInHz;

    public ToneManager(Context context, View toneView, int index) {
        this.context = context;
        this.toneView = toneView;
        this.index = index;
        initializeUIElements();
        initializeUIValues();
        setFundamentalFrequency();
        initializeOvertoneManagers();
        initializeUIListeners();
    }

    public SineWave[] getSineWaves() {
        int activeOvertonesNumber = getActiveOvertonesNumber();
        SineWave[] sineWaves = new SineWave[activeOvertonesNumber + 1];
        sineWaves[0] = new SineWave(fundamentalFrequencyInHz, getAmplitude());
        if (activeOvertonesNumber > 0) {
            SineWave[] overtones = getOvertonesSineWaves(activeOvertonesNumber);
            System.arraycopy(overtones, 0, sineWaves, 1, overtones.length);
        }
        return sineWaves;
    }

    public static void changeToCustomPreset(int toneIndex) {

    }

    private int getFrequency() {
        return 0;
        //TODO: return tone frequency
    }

    private double getAmplitude() {
        int displayAmplitude = Integer.parseInt(masterAmplitudeInput.getText().toString());
        return (double) displayAmplitude / 100.0d;
    }

    private SineWave[] getOvertonesSineWaves(int activeOvertonesNumber) {
        SineWave[] overtones = new SineWave[activeOvertonesNumber];
        int overtoneIndex = 0;
        for (OvertoneManager overtoneManager : overtoneManagers) {
            if (overtoneManager.isActive()) {
                overtones[overtoneIndex] = overtoneManager.getSineWave();
                ++overtoneIndex;
            }
        }
        return overtones;
    }

    private int getActiveOvertonesNumber() {
        int activeOvertonesNumber = 0;
        if (!overtonesActivator.isChecked())
            return activeOvertonesNumber;
        for (OvertoneManager overtoneManager : overtoneManagers) {
            if (overtoneManager.isActive())
                ++activeOvertonesNumber;
        }
        return activeOvertonesNumber;
    }

    private void setFundamentalFrequency() {
        validateFundamentalFrequencyInput();
        fundamentalFrequencyInHz = Integer.parseInt(fundamentalFrequencyInput.getText().toString());
    }

    private void initializeUIElements() {
        fundamentalFrequencyInput = toneView.findViewById(R.id.fundamental_frequency_input);
        fundamentalFrequencyBar = toneView.findViewById(R.id.fundamental_frequency_bar);
        frequencyDecrementBtn = toneView.findViewById(R.id.frequency_decrement_btn);
        frequencyIncrementBtn = toneView.findViewById(R.id.frequency_increment_btn);
        masterAmplitudeBar = toneView.findViewById(R.id.master_volume_bar);
        masterAmplitudeInput = toneView.findViewById(R.id.master_volume_input);
        overtonesActivator = toneView.findViewById(R.id.overtones_activator);
        overtonesPreset = toneView.findViewById(R.id.overtones_preset);
        overtonesLayout = toneView.findViewById(R.id.overtones_layout);
    }

    private void initializeOvertoneManagers() {
        overtoneManagers = new OvertoneManager[Config.OVERTONES_NUMBER.value];
        for (int i = 0; i < overtoneManagers.length; ++i) {
            int overtoneIndex = i + 1;
            int overtoneFrequency = fundamentalFrequencyInHz * (overtoneIndex + 1);
            String ID = "overtone_" + overtoneIndex;
            int resID = context.getResources().getIdentifier(ID, "id", context.getPackageName());
            View overtoneView = toneView.findViewById(resID);
            overtoneManagers[i] = new OvertoneManager(overtoneView, overtoneIndex, overtoneFrequency, getPresetForTone(), index);
        }
    }

    private void initializeUIValues() {
        int displayFrequency = UnitsConverter.convertSeekBarPositionToFrequency(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        fundamentalFrequencyInput.setText(String.valueOf(displayFrequency));
        fundamentalFrequencyBar.setMax(Config.FREQUENCY_PROGRESS_BAR_MAX.value);
        fundamentalFrequencyBar.setProgress(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        masterAmplitudeBar.setProgress(100);
        masterAmplitudeInput.setText(String.valueOf(masterAmplitudeBar.getProgress()));

        // Spinners
        ArrayAdapter<CharSequence> overtonesPresetAdapter = ArrayAdapter.createFromResource(context, R.array.overtones_presets_array, android.R.layout.simple_spinner_item);
        overtonesPresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overtonesPreset.setAdapter(overtonesPresetAdapter);

        // Layout visibility
        overtonesLayout.setVisibility(View.GONE);
        overtonesPreset.setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUIListeners() {
        fundamentalFrequencyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int frequency = Integer.parseInt(s.toString());
                    if (frequency >= Config.FREQUENCY_MIN.value && frequency <= Config.FREQUENCY_MAX.value)
                        fundamentalFrequencyBar.setProgress(UnitsConverter.convertFrequencyToSeekBarPosition(frequency));
                    else if (frequency > Config.FREQUENCY_MAX.value)
                        fundamentalFrequencyInput.setText(String.valueOf(Config.FREQUENCY_MAX.value));
                    else
                        fundamentalFrequencyBar.setProgress(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isFundamentalFrequencyInputValid()) {
                    setFundamentalFrequency();
                    initializeOvertoneManagers();
                }

            }
        });

        fundamentalFrequencyInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    validateFundamentalFrequencyInput();
            }
        });

        fundamentalFrequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    fundamentalFrequencyInput.setText(String.valueOf(UnitsConverter.convertSeekBarPositionToFrequency(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        frequencyDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(fundamentalFrequencyInput.getText().toString());
                if (--frequency >= Config.FREQUENCY_MIN.value) {
                    fundamentalFrequencyBar.setProgress(UnitsConverter.convertFrequencyToSeekBarPosition(frequency));
                    fundamentalFrequencyInput.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), fundamentalFrequencyInput, Options.Operation.FREQUENCY_DECREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        frequencyDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    validateFundamentalFrequencyInput();
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        frequencyIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(fundamentalFrequencyInput.getText().toString());
                if (++frequency <= Config.FREQUENCY_MAX.value) {
                    fundamentalFrequencyBar.setProgress(UnitsConverter.convertFrequencyToSeekBarPosition(frequency));
                    fundamentalFrequencyInput.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), fundamentalFrequencyInput, Options.Operation.FREQUENCY_INCREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        frequencyIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    validateFundamentalFrequencyInput();
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        masterAmplitudeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                masterAmplitudeInput.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtonesActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    overtonesLayout.setVisibility(View.VISIBLE);
                    overtonesPreset.setVisibility(View.VISIBLE);
                    return;
                }
                overtonesLayout.setVisibility(View.GONE);
                overtonesPreset.setVisibility(View.GONE);
            }
        });

        overtonesPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Preset targetOvertonesPreset = Options.convertStringToPreset(overtonesPreset.getSelectedItem().toString());
                Preset currentOvertonesPreset = getPresetForTone();
                if (targetOvertonesPreset == currentOvertonesPreset)
                    return;
                switch (index) {
                    case 1:
                        Options.tone1Preset = targetOvertonesPreset;
                        break;
                    case 2:
                        Options.tone2Preset = targetOvertonesPreset;
                        break;
                }
                initializeOvertoneManagers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private Preset getPresetForTone() {
        switch (index) {
            case 1:
                return Options.tone1Preset;
            case 2:
                return Options.tone2Preset;
        }
        return Preset.FLAT;
    }

    private void validateFundamentalFrequencyInput() {
        int displayFrequency = UnitsConverter.convertSeekBarPositionToFrequency(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        try {
            displayFrequency = Integer.parseInt(fundamentalFrequencyInput.getText().toString());
            if (displayFrequency < Config.FREQUENCY_MIN.value)
                fundamentalFrequencyInput.setText(String.valueOf(Config.FREQUENCY_MIN.value));
        } catch (Exception e) {
            Toast.makeText(context, "Frequency error", Toast.LENGTH_SHORT).show();
            fundamentalFrequencyInput.setText(String.valueOf(displayFrequency));
        }
    }

    private boolean isFundamentalFrequencyInputValid() {
        int frequency = 0;
        try {
            frequency = Integer.parseInt(fundamentalFrequencyInput.getText().toString());
        } catch (Exception e) {
            return false;
        }
        return frequency >= Config.FREQUENCY_MIN.value && frequency <= Config.FREQUENCY_MAX.value;
    }
}
