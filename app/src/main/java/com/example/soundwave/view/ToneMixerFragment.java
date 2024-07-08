package com.example.soundwave.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.soundwave.R;
import com.example.soundwave.Tone;
import com.example.soundwave.additionalviews.OnToneSelectedListener;
import com.example.soundwave.additionalviews.SelectToneToMixDialogFragment;
import com.example.soundwave.databinding.FragmentToneMixerBinding;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneMixerViewModel;

import java.util.Locale;

public class ToneMixerFragment extends Fragment implements OnToneSelectedListener {

    private ToneMixerViewModel viewModel;
    private FragmentToneMixerBinding binding;

    private View.OnLongClickListener toneWorkbenchLongClickListener;
    private View.OnLongClickListener toneTrackLongClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneMixerBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ToneMixerViewModel.class);

        setupScale();
        setupDragAndDrop();
        setupLongClickListeners();
        setOnClickListeners();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onToneSelected(Tone tone) {
        addToneToWorkbench(tone);
    }

    private void setupScale() {
        int totalLabels = 25;

        for (int i = 0; i <= totalLabels; i++) {
            TextView label = new TextView(requireContext());
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f);
            label.setText(String.format(Locale.US, "|%.1fs", i/2.0f));   // i/2.0f = 0.5s every Config.TONE_MIXER_SCALE_LABEL_WIDTH_DP.value

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Config.TONE_MIXER_SCALE_LABEL_WIDTH_DP.value,
                            getResources().getDisplayMetrics()), ViewGroup.LayoutParams.WRAP_CONTENT
            );
            label.setLayoutParams(params);
            binding.toneMixerTimeline.addView(label);
        }
    }

    private void setupDragAndDrop() {
        View.OnDragListener dragListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        View draggedView = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) draggedView.getParent();
                        owner.removeView(draggedView);

                        ((LinearLayout) v).addView(draggedView);
                        draggedView.setVisibility(View.VISIBLE);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!event.getResult()) {
                            View droppedView = (View) event.getLocalState();
                            droppedView.setVisibility(View.VISIBLE);
                        }
                        return true;
                    default:
                        break;
                }
                return false;
            }
        };

        binding.toneMixerTrack1.setOnDragListener(dragListener);
        binding.toneMixerTrack2.setOnDragListener(dragListener);
        binding.toneMixerTrack3.setOnDragListener(dragListener);
        binding.toneMixerTrack4.setOnDragListener(dragListener);
        binding.toneMixerTrack5.setOnDragListener(dragListener);
    }

    private void setupLongClickListeners() {
        toneWorkbenchLongClickListener = v -> {
            View trackTone = createTrackTone(v);
            trackTone.setOnLongClickListener(toneTrackLongClickListener);
            binding.toneMixerTrack1.addView(trackTone);

            return true;
        };

        toneTrackLongClickListener = v -> {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(null, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);

            return true;
        };
    }

    private void setOnClickListeners() {
        binding.toneMixerAddToneBtn.setOnClickListener(v -> {
            SelectToneToMixDialogFragment dialog = new SelectToneToMixDialogFragment(this);
            dialog.show(getParentFragmentManager(), "SelectToneToMixDialogFragment");
        });
    }

    private void addToneToWorkbench(Tone tone) {
        View workbenchTone = createWorkbenchTone(tone);
        workbenchTone.setOnLongClickListener(toneWorkbenchLongClickListener);
        binding.toneMixerWorkbench.addView(workbenchTone);
    }

    private View createWorkbenchTone(Tone tone) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View workbenchTone = inflater.inflate(R.layout.tone_mixer_workbench_tone, binding.toneMixerWorkbench, false);

        TextView toneName = workbenchTone.findViewById(R.id.tone_mixer_workbench_tone_name);
        TextView toneFrequency = workbenchTone.findViewById(R.id.tone_mixer_workbench_tone_frequency);
        TextView toneVolume = workbenchTone.findViewById(R.id.tone_mixer_workbench_tone_volume);
        TextView toneDuration = workbenchTone.findViewById(R.id.tone_mixer_workbench_tone_duration);

        int frequency = tone.getFundamentalFrequency();

        String name = tone.getName();
        String scale = UnitsConverter.convertFrequencyToNote(frequency);
        String frequencyDisplay = frequency + getString(R.string.affix_Hz) + " (" + scale + ")";
        String volume = tone.getMasterVolume() + getString(R.string.affix_percent);
        String duration = String.format(Locale.US, "%.3fs", tone.getDurationInSeconds());

        toneName.setSelected(true);
        toneName.setText(name);
        toneFrequency.setText(frequencyDisplay);
        toneVolume.setText(volume);
        toneDuration.setText(duration);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.tone_mixer_tone_width),
                (int) getResources().getDimension(R.dimen.tone_mixer_workbench_tone_height)
        );
        layoutParams.setMargins(
                (int) getResources().getDimension(R.dimen.tone_mixer_tone_workbench_margin_left),
                (int) getResources().getDimension(R.dimen.tone_mixer_tone_workbench_margin_top),
                (int) getResources().getDimension(R.dimen.tone_mixer_tone_workbench_margin_right),
                (int) getResources().getDimension(R.dimen.tone_mixer_tone_workbench_margin_bottom)
        );
        workbenchTone.setLayoutParams(layoutParams);
        workbenchTone.setTag(tone);

        return workbenchTone;
    }

    private View createTrackTone(View v) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View trackTone = inflater.inflate(R.layout.tone_mixer_track_tone, binding.toneMixerTrack1, false);

        Tone tone = (Tone) v.getTag();
        trackTone.setTag(tone);

        TextView mixerName = trackTone.findViewById(R.id.tone_mixer_track_tone_name);
        mixerName.setText(tone.getName());

        int widthInDp = (int) Math.round(2 * Config.TONE_MIXER_SCALE_LABEL_WIDTH_DP.value * tone.getDurationInSeconds());
        int widthInPx = UnitsConverter.dpToPx(widthInDp);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                widthInPx,
                (int) getResources().getDimension(R.dimen.tone_mixer_track_tone_height)
        );
        trackTone.setLayoutParams(layoutParams);

        return trackTone;
    }

    private int calculateDropIndex(LinearLayout parent, int dropX) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int childLeft = child.getLeft();
            int childRight = child.getRight();
            if (dropX < childLeft + child.getWidth() / 2) {
                return i;  // Umieść przed tym elementem
            } else if (dropX > childRight - child.getWidth() / 2) {
                continue;  // Sprawdź kolejny element
            } else {
                return i + 1;  // Umieść za tym elementem
            }
        }
        return parent.getChildCount();  // Umieść na końcu
    }
}