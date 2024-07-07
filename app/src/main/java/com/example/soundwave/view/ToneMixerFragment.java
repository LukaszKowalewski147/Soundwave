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
import com.example.soundwave.viewmodel.ToneMixerViewModel;

import java.util.Locale;
import java.util.Random;

public class ToneMixerFragment extends Fragment implements OnToneSelectedListener {

    private ToneMixerViewModel viewModel;
    private FragmentToneMixerBinding binding;

    private View.OnLongClickListener longClickListenerInTonesList;
    private View.OnLongClickListener longClickListenerInMixing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneMixerBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ToneMixerViewModel.class);

        setupScale();
        setupDragAndDrop();
        setupAddToneButton();
        setupLongClickListeners();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void setupAddToneButton() {
        binding.toneMixerAddToneBtn.setOnClickListener(v -> openSelectToneToMixDialog());
    }

    private void openSelectToneToMixDialog() {
        SelectToneToMixDialogFragment dialog = new SelectToneToMixDialogFragment(this);
        dialog.show(getParentFragmentManager(), "SelectToneToMixDialogFragment");
    }

    private void addToneToTonesHolder(Tone tone) {
        View tonePrototype = createWorkbenchTonePrototype(tone);
        tonePrototype.setOnLongClickListener(longClickListenerInTonesList);
        binding.toneMixerWorkbench.addView(tonePrototype);
    }

    private View createWorkbenchTonePrototype(Tone tone) {
        View tonePrototype = createWorkbenchToneEmptyPrototype();

        tonePrototype.setTag(tone);

        TextView name = tonePrototype.findViewById(R.id.tone_mixer_workbench_tone_name);
        name.setText(tone.getName());

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
        tonePrototype.setLayoutParams(layoutParams);

        return tonePrototype;
    }

    private View createWorkbenchToneEmptyPrototype() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        return inflater.inflate(R.layout.tone_mixer_workbench_tone, binding.toneMixerWorkbench, false);
    }

    private View createTrackToneEmptyPrototype() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        return inflater.inflate(R.layout.tone_mixer_track_tone, binding.toneMixerTrack1, false);
    }

    private void setupScale() {
        int labelWidthDp = 40;  // Szerokość jednej etykiety skali w dp
        int totalLabels = 25;   // Całkowita liczba etykiet skali

        for (int i = 0; i <= totalLabels; i++) {
            TextView label = new TextView(requireContext());
            label.setTextSize(12.0f);
            label.setText(String.format(Locale.US, "%.1fs", (i * 500)/1000.0f));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, labelWidthDp, getResources().getDisplayMetrics()),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            label.setLayoutParams(params);
            binding.toneMixerTimeline.addView(label);
        }
    }

    private void setupLongClickListeners() {
        longClickListenerInTonesList = v -> {
            View trackTone = createTrackToneEmptyPrototype();

            Tone tone = (Tone) v.getTag();
            trackTone.setTag(tone);

            TextView mixerName = trackTone.findViewById(R.id.tone_mixer_track_tone_name);
            mixerName.setText(tone.getName());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.tone_mixer_tone_width),
                    (int) getResources().getDimension(R.dimen.tone_mixer_track_tone_height)
            );
            trackTone.setLayoutParams(layoutParams);

            trackTone.setOnLongClickListener(longClickListenerInMixing);
            binding.toneMixerTrack1.addView(trackTone);
            return true;
        };

        longClickListenerInMixing = v -> {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(null, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        };
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

    private int randomizeIcon() {
        int iconID = R.drawable.ic_home;
        int randomIcon = new Random().nextInt(4);

        switch (randomIcon) {
            case 0:
                iconID = R.drawable.ic_duration;
                break;
            case 1:
                iconID = R.drawable.ic_tone_creator;
                break;
            case 2:
                iconID = R.drawable.ic_delete;
                break;
            case 3:
                iconID = R.drawable.ic_save;
                break;
        }
        return iconID;
    }

    @Override
    public void onToneSelected(Tone tone) {
        addToneToTonesHolder(tone);
    }
}