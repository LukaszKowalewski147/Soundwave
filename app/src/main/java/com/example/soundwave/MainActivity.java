package com.example.soundwave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton frequencyApplyBtn;
    private AppCompatButton frequencyDecrementBtn;
    private AppCompatButton frequencyIncrementBtn;
    private AppCompatButton durationApplyBtn;
    private AppCompatButton durationDecrementBtn;
    private AppCompatButton durationIncrementBtn;
    private AppCompatButton loadBtn;
    private AppCompatButton saveBtn;
    private ImageButton playPauseBtn;
    private ImageButton replayBtn;
    private ImageButton loopBtn;
    private ImageView loopIndicator;
    private TextView frequencyDetails;
    private TextView durationDetails;
    private TextView samplingRateDetails;
    private TextView playbackElapsedTime;
    private TextView playbackTotalTime;
    private EditText frequencyTxt;
    private EditText durationTxt;
    private SeekBar frequencyBar;
    private SeekBar durationBar;
    private SeekBar playbackBar;
    private Spinner playbackModesSpinner;
    private Spinner samplingRatesSpinner;
    //private AppCompatButton extraBtn;

    private Tone tone;
    private PlaybackManager playbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tone = null;
        playbackManager = null;

        initializeUIElements();
        initializeUIListeners();
        initializePlaybackManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetPlayback();
    }

    private void loadTone() {
        int frequency = getFrequency();
        short duration = getDuration();
        SamplingRate samplingRate = getSamplingRate();
        tone = new SoundGenerator(frequency, duration, samplingRate).generateTone();
        playbackManager.setTone(tone);

        frequencyDetails.setText(frequency + "Hz");
        durationDetails.setText(duration + "s");
        samplingRateDetails.setText(samplingRatesSpinner.getSelectedItem().toString());
        playbackTotalTime.setText(String.valueOf(duration));
        playbackBar.setMax(samplingRate.samplingRate/100); // one step every 100 samples
    }

    private void managePlayPauseActivity() {
        playbackManager.managePlayPauseActivity();
        if (Options.playbackState == Options.PlaybackState.ON) {
            Thread playbackManagerThread = new Thread(playbackManager);
            playbackManagerThread.start();
        }
    }

    private void resetPlayback() {
        playbackManager.resetPlayback();
    }

    private void saveTone() {
        WavCreator wavCreator = new WavCreator(this, tone);
        wavCreator.saveTone();
    }

    private void manageLoopButton() {
        if (loopIndicator.getVisibility() == View.INVISIBLE) {
            loopIndicator.setVisibility(View.VISIBLE);
            Options.looperState = Options.LooperState.ON;
        } else {
            loopIndicator.setVisibility(View.INVISIBLE);
            Options.looperState = Options.LooperState.OFF;
        }
    }

    private void extra() {

        //Toast.makeText(this, "threads: " + Thread.activeCount(), Toast.LENGTH_SHORT).show();

        playbackManager.extra();
    }

    private int getFrequency() {
        return Integer.parseInt(frequencyTxt.getText().toString());
    }

    private short getDuration() {
        return (short) durationBar.getProgress();
    }

    private SamplingRate getSamplingRate() {
        SamplingRate samplingRate = SamplingRate.RATE_44_1_KHZ;
        String selectedSamplingRate = samplingRatesSpinner.getSelectedItem().toString();
        switch (selectedSamplingRate) {
            case "48kHz":
                samplingRate = SamplingRate.RATE_48_KHZ;
                break;
            case "96kHz":
                samplingRate = SamplingRate.RATE_96_KHZ;
                break;
            case "192kHz":
                samplingRate = SamplingRate.RATE_192_KHZ;
                break;
        }
        return samplingRate;
    }

    private void initializePlaybackManager() {
        int frequency = getFrequency();
        short duration = getDuration();
        SamplingRate samplingRate = getSamplingRate();
        tone = new SoundGenerator(frequency, duration, samplingRate).generateTone();

        Handler handler = new Handler();
        playbackManager = new PlaybackManager(this, handler, playPauseBtn, playbackBar, playbackElapsedTime);
        playbackManager.setTone(tone);

        frequencyDetails.setText(frequency + "Hz");
        durationDetails.setText(duration + "s");
        playbackTotalTime.setText(String.valueOf(duration));
        playbackBar.setMax(samplingRate.samplingRate/100); // one step every 100 samples
    }

    private void initializeUIElements() {
        frequencyBar = findViewById(R.id.frequency_bar);
        durationBar = findViewById(R.id.duration_bar);
        frequencyTxt = findViewById(R.id.frequency_txt);
        durationTxt = findViewById(R.id.duration_txt);
        frequencyApplyBtn = findViewById(R.id.frequency_apply_btn);
        frequencyDecrementBtn = findViewById(R.id.frequency_decrement_btn);
        frequencyIncrementBtn = findViewById(R.id.frequency_increment_btn);
        durationApplyBtn = findViewById(R.id.duration_apply_btn);
        durationDecrementBtn = findViewById(R.id.duration_decrement_btn);
        durationIncrementBtn = findViewById(R.id.duration_increment_btn);
        loadBtn = findViewById(R.id.load_btn);
        saveBtn = findViewById(R.id.save_btn);
        frequencyDetails = findViewById(R.id.tone_details_frequency);
        durationDetails = findViewById(R.id.tone_details_duration);
        samplingRateDetails = findViewById(R.id.tone_details_sampling);
        playbackBar = findViewById(R.id.playback_bar);
        playbackElapsedTime = findViewById(R.id.playback_elapsed_time);
        playbackTotalTime = findViewById(R.id.playback_total_time);
        playPauseBtn = findViewById(R.id.play_pause_btn);
        replayBtn = findViewById(R.id.replay_btn);
        loopBtn = findViewById(R.id.loop_btn);
        loopIndicator = findViewById(R.id.loop_indicator);
        playbackModesSpinner = findViewById(R.id.playback_modes_spinner);
        samplingRatesSpinner = findViewById(R.id.sampling_rates_spinner);

        ArrayAdapter<CharSequence> playbackModesAdapter = ArrayAdapter.createFromResource(this, R.array.playback_modes_array, android.R.layout.simple_spinner_item);
        playbackModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playbackModesSpinner.setAdapter(playbackModesAdapter);

        ArrayAdapter<CharSequence> samplingRatesAdapter = ArrayAdapter.createFromResource(this, R.array.sampling_rates_array, android.R.layout.simple_spinner_item);
        samplingRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        samplingRatesSpinner.setAdapter(samplingRatesAdapter);

        //extraBtn = findViewById(R.id.extra_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            durationBar.setMin(Constants.DURATION_MIN.value);
        }
        frequencyBar.setMax(Constants.FREQUENCY_PROGRESS_BAR_MAX.value);
        durationBar.setMax(Constants.DURATION_MAX.value);

        frequencyBar.setProgress(Constants.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        durationBar.setProgress(Constants.DURATION_DEFAULT.value);

        frequencyTxt.setText(String.valueOf(UnitsConverter.convertProgressBarPositionToFrequency(Constants.FREQUENCY_PROGRESS_BAR_DEFAULT.value)));
        durationTxt.setText(String.valueOf(Constants.DURATION_DEFAULT.value));

        loopIndicator.setVisibility(View.INVISIBLE);
    }

    private void initializeUIListeners() {
        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frequencyTxt.setText(String.valueOf(UnitsConverter.convertProgressBarPositionToFrequency(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                durationTxt.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        frequencyApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());

                if (frequency >= Constants.FREQUENCY_MIN.value && frequency <= Constants.FREQUENCY_MAX.value) {
                    frequencyBar.setProgress(UnitsConverter.convertFrequencyToProgressBarPosition(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
                else if (frequency > Constants.FREQUENCY_MAX.value) {
                    frequencyBar.setProgress(Constants.FREQUENCY_PROGRESS_BAR_MAX.value);
                    frequencyTxt.setText(String.valueOf(Constants.FREQUENCY_MAX.value));
                }
                else {
                    frequencyBar.setProgress(0);
                    frequencyTxt.setText(String.valueOf(Constants.FREQUENCY_MIN.value));
                }

            }
        });

        durationApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());

                if (duration >= Constants.DURATION_MIN.value && duration <= Constants.DURATION_MAX.value)
                    durationBar.setProgress(duration);
                else if (duration > Constants.DURATION_MAX.value) {
                    durationBar.setProgress(Constants.DURATION_MAX.value);
                    durationTxt.setText(String.valueOf(Constants.DURATION_MAX.value));
                }
                else {
                    durationBar.setProgress(Constants.DURATION_MIN.value);
                    durationTxt.setText(String.valueOf(Constants.DURATION_MIN.value));
                }
            }
        });

        frequencyDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());
                if (--frequency >= Constants.FREQUENCY_MIN.value) {
                    frequencyBar.setProgress(UnitsConverter.convertFrequencyToProgressBarPosition(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, frequencyBar, frequencyTxt, Options.Operation.FREQUENCY_DECREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        frequencyDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        frequencyIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());
                if (++frequency <= Constants.FREQUENCY_MAX.value) {
                    frequencyBar.setProgress(UnitsConverter.convertFrequencyToProgressBarPosition(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, frequencyBar, frequencyTxt, Options.Operation.FREQUENCY_INCREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        frequencyIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        durationDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());
                if (--duration >= Constants.DURATION_MIN.value) {
                    durationBar.setProgress(duration);
                    durationTxt.setText(String.valueOf(duration));
                }
            }
        });

        durationDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonDecrementDurationState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, durationBar, Options.Operation.DURATION_DECREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        durationDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementDurationState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        durationIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());
                if (++duration <= Constants.DURATION_MAX.value) {
                    durationBar.setProgress(duration);
                    durationTxt.setText(String.valueOf(duration));
                }
            }
        });

        durationIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonIncrementDurationState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, durationBar, Options.Operation.DURATION_INCREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        durationIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementDurationState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTone();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTone();
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePlayPauseActivity();
            }
        });

        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPlayback();
            }
        });

        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageLoopButton();
            }
        });

        playbackBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
/*
        extraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extra();
            }
        });
 */
    }
}