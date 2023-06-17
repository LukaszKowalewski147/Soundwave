package com.example.soundwave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private SeekBar frequencyBar;
    private SeekBar durationBar;
    private EditText frequencyTxt;
    private EditText durationTxt;
    private AppCompatButton frequencyApplyBtn;
    private AppCompatButton frequencyDecrementBtn;
    private AppCompatButton frequencyIncrementBtn;
    private AppCompatButton durationApplyBtn;
    private AppCompatButton durationDecrementBtn;
    private AppCompatButton durationIncrementBtn;
    private ImageButton playBtn;
    private ImageButton pauseBtn;
    private ImageButton stopBtn;
    private AppCompatButton saveBtn;

    private Tone tone;
    private TonePlayer tonePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tone = null;
        tonePlayer = null;

        initializeUIElements();
        initializeUIListeners();
    }

    private void startPlayback() {
        stopPlayback();
        int frequency = getFrequency();
        short duration = getDuration();
        tone = new SoundGenerator(frequency, duration).generateTone();
        tonePlayer = new TonePlayer(tone);
        tonePlayer.play(this);
    }

    private void pausePlayback() {
        if (tonePlayer != null)
            tonePlayer.pause();
    }

    private void stopPlayback() {
        if (tonePlayer != null)
            tonePlayer.stop();
        tonePlayer = null;
        tone = null;
    }

    private void saveTone() {
        stopPlayback();
        int frequency = getFrequency();
        short duration = getDuration();
        tone = new SoundGenerator(frequency, duration).generateTone();
        WavCreator wavCreator = new WavCreator(this, tone);
        wavCreator.saveTone();
    }

    private int getFrequency() {
        return Integer.parseInt(frequencyTxt.getText().toString());
    }

    private short getDuration() {
        return (short) durationBar.getProgress();
    }

    private int convertFromSlider(int sliderValue) {
        int output = 0;
        double fractionOutput = 0.0d;
        double toTenthPower = 0.0d;

        sliderValue += 500;
        toTenthPower = sliderValue/500.0d;
        fractionOutput = Math.pow(toTenthPower, 10);
        output = (int) Math.round(fractionOutput);  // 1 is the lowest possible
        output += 19;                               // thats why + 19 to match 20Hz minimum

        if (output > Constants.FREQ_MAX.value)
            output = Constants.FREQ_MAX.value;

        return output;
    }

    private int convertToSlider(int input) {
        int output = 0;
        double fractionOutput = 0.0d;
        double toTenthRoot = 0.0d;

        input -= 19;
        toTenthRoot = Math.pow(input, (double) 1/10);
        fractionOutput = toTenthRoot * 500;
        fractionOutput -= 500;
        output = (int) Math.round(fractionOutput);

        return output;
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
        playBtn = findViewById(R.id.play_btn);
        pauseBtn = findViewById(R.id.pause_btn);
        stopBtn = findViewById(R.id.stop_btn);
        saveBtn = findViewById(R.id.save_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            frequencyBar.setMin(Constants.FREQ_SLIDER_MIN.value);
            durationBar.setMin(Constants.DURATION_MIN.value);
        }
        frequencyBar.setMax(Constants.FREQ_SLIDER_MAX.value);
        durationBar.setMax(Constants.DURATION_MAX.value);

        frequencyBar.setProgress(Constants.FREQ_SLIDER_START.value);
        durationBar.setProgress(Constants.DURATION_START.value);

        frequencyTxt.setText(String.valueOf(convertFromSlider(Constants.FREQ_SLIDER_START.value)));
        durationTxt.setText(String.valueOf(Constants.DURATION_START.value));
    }

    private void initializeUIListeners() {
        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frequencyTxt.setText(String.valueOf(convertFromSlider(progress)));
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

                if (frequency >= Constants.FREQ_MIN.value && frequency <= Constants.FREQ_MAX.value) {
                    frequencyBar.setProgress(convertToSlider(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
                else if (frequency > Constants.FREQ_MAX.value) {
                    frequencyBar.setProgress(Constants.FREQ_SLIDER_MAX.value);
                    frequencyTxt.setText(String.valueOf(Constants.FREQ_MAX.value));
                }
                else {
                    frequencyBar.setProgress(Constants.FREQ_SLIDER_MIN.value);
                    frequencyTxt.setText(String.valueOf(Constants.FREQ_MIN.value));
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
                if (--frequency >= Constants.FREQ_MIN.value) {
                    frequencyBar.setProgress(convertToSlider(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());
                if (++frequency <= Constants.FREQ_MAX.value) {
                    frequencyBar.setProgress(convertToSlider(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
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

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayback();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlayback();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayback();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTone();
            }
        });
    }
}