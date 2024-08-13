package com.example.soundwave.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soundwave.MainActivity;
import com.example.soundwave.R;
import com.example.soundwave.components.ControlPanelComponent;
import com.example.soundwave.components.Tone;
import com.example.soundwave.additionalviews.OnToneSelectedListener;
import com.example.soundwave.additionalviews.SelectToneToMixDialogFragment;
import com.example.soundwave.databinding.FragmentToneMixerBinding;
import com.example.soundwave.utils.OnFragmentExitListener;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.TrackData;
import com.example.soundwave.utils.TrackToneData;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneMixerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ToneMixerFragment extends Fragment implements OnToneSelectedListener, OnFragmentExitListener {
    private final String TAG = "ToneMixerFragment";

    private ToneMixerViewModel viewModel;
    private FragmentToneMixerBinding binding;

    private View.OnTouchListener mainTouchListener;
    private View.OnDragListener trackToneDragListener;
    private View.OnDragListener trackToneRemoveDragListener;
    private View.OnLongClickListener toneWorkbenchLongClickListener;
    private View.OnLongClickListener toneTrackLongClickListener;
    private View.OnClickListener toneClickListener;
    private View.OnClickListener toneRemoveClickListener;

    private View toneToRemove;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneMixerBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ToneMixerViewModel.class);

        setupScale();
        setupTracks();
        setupListeners();

        setObservers();
        setListeners();

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
        int totalLabels = 50;

        for (int i = 0; i <= totalLabels; i++) {
            TextView label = new TextView(requireContext());
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
            label.setText(String.format(Locale.US, "|%.1fs", i / 2.0f));   // i/2.0f = 0.5s every R.dimen.tone_mixer_scale_label_width

            int width = getResources().getDimensionPixelSize(R.dimen.tone_mixer_scale_label_width);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    width, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            label.setLayoutParams(params);
            binding.toneMixerTimeline.addView(label);
        }
    }

    private void setupTracks() {
        binding.toneMixerTrack1.setTag(R.id.tag_track_number, 1);
        binding.toneMixerTrack1.setTag(R.id.tag_track_droppable, true);
        binding.toneMixerTrack2.setTag(R.id.tag_track_number, 2);
        binding.toneMixerTrack2.setTag(R.id.tag_track_droppable, true);
        binding.toneMixerTrack3.setTag(R.id.tag_track_number, 3);
        binding.toneMixerTrack3.setTag(R.id.tag_track_droppable, true);
        binding.toneMixerTrack4.setTag(R.id.tag_track_number, 4);
        binding.toneMixerTrack4.setTag(R.id.tag_track_droppable, true);
        binding.toneMixerTrack5.setTag(R.id.tag_track_number, 5);
        binding.toneMixerTrack5.setTag(R.id.tag_track_droppable, true);
    }

    private void setupListeners() {
        mainTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (toneToRemove != null) {
                    Object tag = v.getTag(R.id.tag_tone_to_remove);
                    if (tag == null || Boolean.FALSE.equals(tag))
                        setToneStateToIrremovable();
                }
            }
            return false;
        };

        trackToneDragListener = (v, event) -> {
            View draggedView;
            ViewGroup owner;
            LinearLayout track;
            boolean toneDroppable;

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    draggedView = (View) event.getLocalState();
                    owner = (ViewGroup) draggedView.getParent();
                    if (owner != null) {
                        track = (LinearLayout) v;
                        if (track == owner) {
                            toneDroppable = isToneDroppable(event, track);
                            track.setTag(R.id.tag_track_droppable, toneDroppable);
                        }
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    track = (LinearLayout) v;
                    boolean droppable = (boolean) track.getTag(R.id.tag_track_droppable);
                    int color = droppable ? ContextCompat.getColor(requireContext(), R.color.green50transparent)
                            : ContextCompat.getColor(requireContext(), R.color.red50transparent);

                    viewModel.stopPreviousTrackAnimation((int) track.getTag(R.id.tag_track_number));
                    track.setBackgroundColor(color);

                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    track = (LinearLayout) v;

                    toneDroppable = isToneDroppable(event, track);
                    track.setTag(R.id.tag_track_droppable, toneDroppable);

                    Drawable background = track.getBackground();
                    int colorFrom = ((ColorDrawable) background).getColor();
                    int colorTo = toneDroppable ? ContextCompat.getColor(requireContext(), R.color.green50transparent)
                            : ContextCompat.getColor(requireContext(), R.color.red50transparent);

                    if (colorFrom != colorTo)
                        track.setBackgroundColor(colorTo);

                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    getDragOutOfTrackAnimation((LinearLayout) v).start();
                    return true;
                case DragEvent.ACTION_DROP:
                    track = (LinearLayout) v;
                    dropToneOnTrack(event, track);
                    getDragOutOfTrackAnimation(track).start();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    View endedView = (View) event.getLocalState();
                    endedView.setVisibility(View.VISIBLE);
                    return true;
                default:
                    break;
            }
            return false;
        };

        trackToneRemoveDragListener = (v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    int enteredColor = ContextCompat.getColor(requireContext(), R.color.delete_bin);
                    binding.toneMixerRemoveTrackToneIcon.setColorFilter(enteredColor);
                    binding.toneMixerRemoveTrackTone.setBackgroundResource(R.drawable.background_mixer_track_tone_remove_active);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    int exitedColor = ContextCompat.getColor(requireContext(), R.color.gray);
                    binding.toneMixerRemoveTrackToneIcon.setColorFilter(exitedColor);
                    binding.toneMixerRemoveTrackTone.setBackgroundResource(R.drawable.background_mixer_track_tone_remove_inactive);
                    return true;
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();

                    if (owner != null) {
                        manageOldPosition(draggedView);
                        viewModel.removeToneFromTrack();
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    binding.toneMixerRemoveTrackTone.setVisibility(View.GONE);
                    int endedColor = ContextCompat.getColor(requireContext(), R.color.gray);
                    binding.toneMixerRemoveTrackToneIcon.setColorFilter(endedColor);
                    binding.toneMixerRemoveTrackTone.setBackgroundResource(R.drawable.background_mixer_track_tone_remove_inactive);
                    binding.toneMixerRemoveTrackTone.setBackgroundResource(R.drawable.background_mixer_track_tone_remove_inactive);
                    return true;
                default:
                    break;
            }
            return false;
        };

        toneWorkbenchLongClickListener = v -> {
            View trackTone = createTrackTone(v);

            getDragWorkbenchToneAnimation().start();

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(trackTone);
            v.startDragAndDrop(null, shadowBuilder, trackTone, 0);

            trackTone.setOnLongClickListener(toneTrackLongClickListener);

            return true;
        };

        toneTrackLongClickListener = v -> {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(null, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);

            binding.toneMixerRemoveTrackTone.setVisibility(View.VISIBLE);

            return true;
        };

        toneClickListener = v -> {
            if (toneToRemove != null)
                setToneStateToIrremovable();
            setToneStateToRemovable(v);
        };

        toneRemoveClickListener = v -> {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent != null) {
                toneToRemove = null;
                parent.removeView(v);
            }
        };
    }

    private void setObservers() {
        viewModel.getMusic().observe(getViewLifecycleOwner(), music -> {

        });

        viewModel.getControlPanelComponent().observe(getViewLifecycleOwner(), this::manageControlPanel);
    }

    private void setListeners() {
        binding.toneMixerMainLayout.setOnTouchListener(mainTouchListener);

        binding.toneMixerMusicScrollView.setOnTouchListener((v, event) -> {
            mainTouchListener.onTouch(v, event); // Return touch event to main layout
            return false;
        });

        binding.toneMixerWorkbenchScrollView.setOnTouchListener((v, event) -> {
            mainTouchListener.onTouch(v, event); // Return touch event to main layout
            return false;
        });

        binding.toneMixerTrack1.setOnDragListener(trackToneDragListener);
        binding.toneMixerTrack2.setOnDragListener(trackToneDragListener);
        binding.toneMixerTrack3.setOnDragListener(trackToneDragListener);
        binding.toneMixerTrack4.setOnDragListener(trackToneDragListener);
        binding.toneMixerTrack5.setOnDragListener(trackToneDragListener);

        binding.toneMixerRemoveTrackTone.setOnDragListener(trackToneRemoveDragListener);

        binding.toneMixerAddToneBtn.setOnClickListener(v -> {
            SelectToneToMixDialogFragment dialog = new SelectToneToMixDialogFragment(this);
            dialog.show(getParentFragmentManager(), "SelectToneToMixDialogFragment");
        });

        binding.toneMixerGenerateMusicBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.alert_dialog_tone_mixer_generate_message);
            builder.setPositiveButton(R.string.alert_dialog_tone_mixer_generate_positive, (dialog, id) -> {
                boolean generationSuccessful = viewModel.generateMusic(getTracksData());

                if (!generationSuccessful) {
                    Log.e(TAG, "Generate music: generation unsuccessful");
                    Toast.makeText(requireContext(), R.string.error_generating_music, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.alert_dialog_tone_mixer_generate_negative, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        binding.toneMixerPlayStopMusicBtn.setOnClickListener(v -> {
            viewModel.playStopMusic();
            animateTimeIndicator();
        });

        binding.toneMixerSaveMusicBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.alert_dialog_tone_mixer_save_message);

            final EditText musicName = new EditText(getContext());
            musicName.setInputType(InputType.TYPE_CLASS_TEXT);

            builder.setView(musicName);

            builder.setPositiveButton(R.string.alert_dialog_tone_mixer_save_positive, null);
            builder.setNegativeButton(R.string.alert_dialog_tone_mixer_save_negative, null);

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String nameToSave = musicName.getText().toString().trim();
                if (!nameToSave.isEmpty()) {
                    viewModel.saveMusic(nameToSave);
                    dialog.dismiss();
                } else {
                    musicName.setError(getString(R.string.error_msg_empty_name));
                }
            }));

            dialog.show();
        });

        binding.toneMixerResetMusicBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.alert_dialog_tone_mixer_reset_message);
            builder.setPositiveButton(R.string.alert_dialog_tone_mixer_reset_positive, (dialog, id) -> takeResetAction());
            builder.setNegativeButton(R.string.alert_dialog_tone_mixer_reset_negative, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void takeResetAction() {
        binding.toneMixerTrack1.removeAllViews();
        binding.toneMixerTrack2.removeAllViews();
        binding.toneMixerTrack3.removeAllViews();
        binding.toneMixerTrack4.removeAllViews();
        binding.toneMixerTrack5.removeAllViews();
        binding.toneMixerWorkbench.removeAllViews();

        viewModel.resetToneMixer();
    }

    private void setToneStateToIrremovable() {
        boolean trackTone = true;
        LinearLayout parent = (LinearLayout) toneToRemove.getParent();
        if (parent.getId() == R.id.tone_mixer_workbench)
            trackTone = false;

        ImageView deleteIcon = toneToRemove.findViewById(R.id.tone_mixer_tone_delete_icon);
        deleteIcon.setVisibility(View.INVISIBLE);
        toneToRemove.setBackgroundResource(trackTone ? R.drawable.background_track_tone : R.drawable.background_workbench_tone);
        toneToRemove.setOnClickListener(toneClickListener);
        toneToRemove.setTag(R.id.tag_tone_to_remove, false);
        toneToRemove = null;
    }

    private void setToneStateToRemovable(View tone) {
        tone.setTag(R.id.tag_tone_to_remove, true);
        ImageView deleteIcon = tone.findViewById(R.id.tone_mixer_tone_delete_icon);
        deleteIcon.setVisibility(View.VISIBLE);
        tone.setBackgroundResource(R.drawable.background_mixer_tone_remove);
        tone.setOnClickListener(toneRemoveClickListener);
        toneToRemove = tone;
    }

    private List<List<Tone>> getTracksData() {
        List<List<Tone>> tracksData = new ArrayList<>();

        tracksData.add(getTonesFromTrack(binding.toneMixerTrack1));
        tracksData.add(getTonesFromTrack(binding.toneMixerTrack2));
        tracksData.add(getTonesFromTrack(binding.toneMixerTrack3));
        tracksData.add(getTonesFromTrack(binding.toneMixerTrack4));
        tracksData.add(getTonesFromTrack(binding.toneMixerTrack5));

        return tracksData;
    }

    private List<Tone> getTonesFromTrack(LinearLayout track) {
        List<Tone> tones = new ArrayList<>();

        for (int i = 0; i < track.getChildCount(); i++) {
            View child = track.getChildAt(i);
            Object toneTag = child.getTag(R.id.tag_tone);

            if (toneTag instanceof Tone)
                tones.add((Tone) toneTag);
            else {
                Object silenceTag = child.getTag(R.id.tag_silence_tone);
                if (Boolean.TRUE.equals(silenceTag)) {
                    double silenceDurationInSeconds = convertWidthInPxToDurationInSeconds(child.getWidth());
                    tones.add(viewModel.generateSilenceTone(silenceDurationInSeconds));
                }
            }
        }
        return tones;
    }

    private void addToneToWorkbench(Tone tone) {
        View workbenchTone = createWorkbenchTone(tone);
        workbenchTone.setOnClickListener(toneClickListener);
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

        workbenchTone.setTag(R.id.tag_tone, tone);
        workbenchTone.setTag(R.id.tag_tone_to_remove, false);
        workbenchTone.setTag(R.id.tag_silence_tone, false);

        return workbenchTone;
    }

    private View createTrackTone(View v) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View trackTone = inflater.inflate(R.layout.tone_mixer_track_tone, binding.toneMixerTrack1, false);

        Tone tone = (Tone) v.getTag(R.id.tag_tone);
        trackTone.setTag(R.id.tag_tone, tone);
        trackTone.setTag(R.id.tag_tone_to_remove, false);
        trackTone.setTag(R.id.tag_silence_tone, false);

        TextView mixerName = trackTone.findViewById(R.id.tone_mixer_track_tone_name);
        mixerName.setText(tone.getName());

        int widthInPx = convertDurationInSecondsToWidthInPx(tone.getDurationInSeconds());
        int heightInPx = (int) getResources().getDimension(R.dimen.tone_mixer_track_tone_height);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
        trackTone.setLayoutParams(layoutParams);

        // Measure and layout the view to ensure it has the correct dimensions to drag from workbench
        int widthSpec = View.MeasureSpec.makeMeasureSpec(widthInPx, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(heightInPx, View.MeasureSpec.EXACTLY);
        trackTone.measure(widthSpec, heightSpec);
        trackTone.layout(0, 0, trackTone.getMeasuredWidth(), trackTone.getMeasuredHeight());

        trackTone.setVisibility(View.INVISIBLE);

        return trackTone;
    }

    private View createSilenceTone(int widthInPx) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View silenceTone = inflater.inflate(R.layout.tone_mixer_track_tone, binding.toneMixerTrack1, false);

        silenceTone.setTag(R.id.tag_tone, null);
        silenceTone.setTag(R.id.tag_tone_to_remove, false);
        silenceTone.setTag(R.id.tag_silence_tone, true);

        int heightInPx = (int) getResources().getDimension(R.dimen.tone_mixer_track_tone_height);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
        silenceTone.setLayoutParams(layoutParams);

        silenceTone.setVisibility(View.INVISIBLE);

        return silenceTone;
    }

    public boolean isToneDroppable(DragEvent event, LinearLayout track) {
        View tone = (View) event.getLocalState();
        int middleX = (int) event.getX();
        int width = tone.getWidth();
        int leftEdge = (int) Math.round(middleX - width / 2.0d);
        int rightEdge = leftEdge + width;

        if (leftEdge < Options.trackPaddingStart)
            return false;

        for (int i = 0; i < track.getChildCount(); i++) {
            View child = track.getChildAt(i);

            Object tag = child.getTag(R.id.tag_silence_tone);
            if (tag == null || Boolean.TRUE.equals(tag))    //  Check if child is silence tone
                continue;

            if (tone.equals(child))
                continue;

            int childLeft = child.getLeft();
            int childRight = child.getRight();

            if (childRight >= leftEdge && childLeft <= rightEdge) {
                return false;
            }
        }
        return true;
    }

    private void dropToneOnTrack(DragEvent event, LinearLayout track) {
        View draggedView = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) draggedView.getParent();

        if (isToneDroppable(event, track)) {
            if (owner != null)
                manageOldPosition(draggedView);

            final int middle = (int) event.getX();
            track.post(() -> addToneToTrack(draggedView, middle, track));
            viewModel.addToneToTrack();
        }
    }

    private void addToneToTrack(View draggedView, int middle, LinearLayout track) {
        TrackData trackData = new TrackData(getTrackChildren(track));
        TrackToneData toneShadow = new TrackToneData(draggedView.getWidth(), middle);

        int[] toneWithSilenceParameters = viewModel.getToneWithSilenceParameters(toneShadow, trackData);

        int toneDropIndex = toneWithSilenceParameters[0];
        int beforeSilenceWidth = toneWithSilenceParameters[1];
        int afterSilenceWidth = toneWithSilenceParameters[2];

        if (viewModel.toneOnTheRightPresent(toneShadow, trackData))
            track.removeViewAt(viewModel.getOldSilenceToneIndex(toneShadow, trackData));

        if (beforeSilenceWidth > 0) {
            View silenceBefore = createSilenceTone(beforeSilenceWidth);
            track.addView(silenceBefore, toneDropIndex++);
        }

        track.addView(draggedView, toneDropIndex++);

        if (afterSilenceWidth > 0) {
            View silenceAfter = createSilenceTone(afterSilenceWidth);
            track.addView(silenceAfter, toneDropIndex);
        }

        draggedView.setVisibility(View.VISIBLE);
    }

    private void manageOldPosition(View draggedView) {
        LinearLayout owner = (LinearLayout) draggedView.getParent();

        int oldTonePosition = owner.indexOfChild(draggedView);
        boolean isSilenceOnTheLeft = isSilence(owner, oldTonePosition - 1);
        boolean isSilenceOnTheRight = isSilence(owner, oldTonePosition + 1);
        boolean isTheLastTone = isTheLastTone(owner, oldTonePosition);

        int replacementSilenceIndex = oldTonePosition;
        int replacementSilenceWidth = draggedView.getWidth();

        if (isSilenceOnTheRight) {
            int silenceOnTheRightIndex = oldTonePosition + 1;
            replacementSilenceWidth += owner.getChildAt(silenceOnTheRightIndex).getWidth();
            owner.removeViewAt(silenceOnTheRightIndex);
        }
        if (isSilenceOnTheLeft) {
            int silenceOnTheLeftIndex = oldTonePosition - 1;
            replacementSilenceWidth += owner.getChildAt(silenceOnTheLeftIndex).getWidth();
            --replacementSilenceIndex;
            owner.removeViewAt(silenceOnTheLeftIndex);
        }

        owner.removeView(draggedView);

        if (!isTheLastTone) {
            View replacementSilence = createSilenceTone(replacementSilenceWidth);
            owner.addView(replacementSilence, replacementSilenceIndex);
        }
    }

    private boolean isSilence(LinearLayout owner, int index) {
        boolean ownerContainsIndex = index >= 0 && index < owner.getChildCount();
        if (!ownerContainsIndex)
            return false;

        View tone = owner.getChildAt(index);
        Object tag = tone.getTag(R.id.tag_silence_tone);
        return Boolean.TRUE.equals(tag);
    }

    private boolean isTheLastTone(LinearLayout owner, int index) {
        return index == owner.getChildCount() - 1;
    }

    private List<TrackToneData> getTrackChildren(LinearLayout track) {
        List<TrackToneData> trackChildren = new ArrayList<>();
        int childCount = track.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = track.getChildAt(i);
            trackChildren.add(new TrackToneData(child.getWidth(), child.getLeft(), child.getRight()));
        }

        return trackChildren;
    }

    private ValueAnimator getDragWorkbenchToneAnimation() {
        int colorFrom = ContextCompat.getColor(requireContext(), R.color.green25transparent);
        int colorTo = ContextCompat.getColor(requireContext(), R.color.transparent);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500);

        colorAnimation.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            binding.toneMixerTrack1.setBackgroundColor(animatedValue);
            binding.toneMixerTrack2.setBackgroundColor(animatedValue);
            binding.toneMixerTrack3.setBackgroundColor(animatedValue);
            binding.toneMixerTrack4.setBackgroundColor(animatedValue);
            binding.toneMixerTrack5.setBackgroundColor(animatedValue);
        });

        return colorAnimation;
    }

    private ValueAnimator getDragOutOfTrackAnimation(LinearLayout track) {
        Drawable background = track.getBackground();
        int colorFrom = ((ColorDrawable) background).getColor();
        int colorTo = ContextCompat.getColor(requireContext(), R.color.transparent);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300);

        colorAnimation.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            track.setBackgroundColor(animatedValue);
        });

        int trackNumber = (int) track.getTag(R.id.tag_track_number);
        viewModel.stopPreviousTrackAnimation(trackNumber);
        viewModel.setCurrentTrackAnimation(trackNumber, colorAnimation);

        return colorAnimation;
    }

    private void animateTimeIndicator() {
        float indicatorStart = getResources().getDimensionPixelSize(R.dimen.tone_mixer_track_padding_start);
        int musicDurationInMs = viewModel.getMusicDurationInMs();
        boolean scrollToAnimate = musicDurationInMs > 1000;

        binding.toneMixerMusicScrollView.scrollTo(0, 0);
        binding.toneMixerTimeIndicator.setX(indicatorStart);
        binding.toneMixerTimeIndicator.setVisibility(View.VISIBLE);

        ObjectAnimator indicatorAnimator;
        ObjectAnimator scrollAnimator = getScrollAnimator(musicDurationInMs);

        if (scrollToAnimate)
            indicatorAnimator = getLongTimeIndicatorAnimator(indicatorStart, scrollAnimator);
        else
            indicatorAnimator = getShortTimeIndicatorAnimator(indicatorStart, musicDurationInMs);

        indicatorAnimator.start();
    }

    private ObjectAnimator getScrollAnimator(int musicDurationInMs) {
        if (musicDurationInMs <= 1000)
            return null;

        double musicDurationInSeconds = UnitsConverter.convertMsToSeconds(musicDurationInMs);
        int scrollStart = binding.toneMixerMusicScrollView.getScrollX();
        int scrollEnd = (int) Math.ceil(getOneSecondWidthPixels() * (musicDurationInSeconds - 1));

        ObjectAnimator scrollAnimator = ObjectAnimator.ofInt(binding.toneMixerMusicScrollView, "scrollX", scrollStart, scrollEnd);
        scrollAnimator.setDuration(musicDurationInMs - 1000);
        scrollAnimator.setInterpolator(new LinearInterpolator());

        scrollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.toneMixerTimeIndicator.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        return scrollAnimator;
    }

    private ObjectAnimator getShortTimeIndicatorAnimator(float indicatorStart, int musicDurationInMs) {
        double musicDurationInSeconds = UnitsConverter.convertMsToSeconds(musicDurationInMs);
        float indicatorEnd = (float) (getOneSecondWidthPixels() * musicDurationInSeconds) + indicatorStart;     // <1s of pixels

        ObjectAnimator indicatorAnimator = ObjectAnimator.ofFloat(binding.toneMixerTimeIndicator, "x", indicatorStart, indicatorEnd);
        indicatorAnimator.setDuration(musicDurationInMs);     // <1s
        indicatorAnimator.setInterpolator(new LinearInterpolator());
        indicatorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.toneMixerTimeIndicator.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        return indicatorAnimator;
    }

    private ObjectAnimator getLongTimeIndicatorAnimator(float indicatorStart, ObjectAnimator scrollAnimator) {
        float indicatorEnd = indicatorStart + getOneSecondWidthPixels();    // 1s of pixels

        ObjectAnimator indicatorAnimator = ObjectAnimator.ofFloat(binding.toneMixerTimeIndicator, "x", indicatorStart, indicatorEnd);
        indicatorAnimator.setDuration(1000);    // 1s of pixels
        indicatorAnimator.setInterpolator(new LinearInterpolator());
        indicatorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scrollAnimator.start();
                super.onAnimationEnd(animation);
            }
        });
        return indicatorAnimator;
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
                    binding.toneMixerGenerateMusicBtn.setEnabled(true);
                    binding.toneMixerGenerateMusicBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    break;
                case INACTIVE:
                    binding.toneMixerGenerateMusicBtn.setEnabled(false);
                    binding.toneMixerGenerateMusicBtn.setBackgroundResource(R.drawable.background_btn_inactive);
            }
        }

        if (playStopBtnState != null) {
            switch (playStopBtnState) {
                case STANDARD:
                    binding.toneMixerPlayStopMusicBtn.setEnabled(true);
                    binding.toneMixerPlayStopMusicBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    binding.toneMixerPlayStopMusicBtn.setText(R.string.play_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_note, null);
                    binding.toneMixerPlayStopMusicBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case INACTIVE:
                    binding.toneMixerPlayStopMusicBtn.setEnabled(false);
                    binding.toneMixerPlayStopMusicBtn.setBackgroundResource(R.drawable.background_btn_inactive);
                    binding.toneMixerPlayStopMusicBtn.setText(R.string.play_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_note, null);
                    binding.toneMixerPlayStopMusicBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case SECOND_FUNCTION:
                    binding.toneMixerPlayStopMusicBtn.setEnabled(true);
                    binding.toneMixerPlayStopMusicBtn.setBackgroundResource(R.drawable.background_btn_red);
                    binding.toneMixerPlayStopMusicBtn.setText(R.string.stop_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_stop, null);
                    binding.toneMixerPlayStopMusicBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }

        if (saveBtnState != null) {
            switch (saveBtnState) {
                case STANDARD:
                    binding.toneMixerSaveMusicBtn.setEnabled(true);
                    binding.toneMixerSaveMusicBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    binding.toneMixerSaveMusicBtn.setText(R.string.save_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_save, null);
                    binding.toneMixerSaveMusicBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case INACTIVE:
                    binding.toneMixerSaveMusicBtn.setEnabled(false);
                    binding.toneMixerSaveMusicBtn.setBackgroundResource(R.drawable.background_btn_inactive);
                    binding.toneMixerSaveMusicBtn.setText(R.string.save_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_save, null);
                    binding.toneMixerSaveMusicBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    break;
                case DONE:
                    binding.toneMixerSaveMusicBtn.setEnabled(false);
                    binding.toneMixerSaveMusicBtn.setBackgroundResource(R.drawable.background_btn_green);
                    binding.toneMixerSaveMusicBtn.setText(R.string.saved_btn);
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done, null);
                    binding.toneMixerSaveMusicBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }

        if (resetBtnState != null) {
            switch (resetBtnState) {
                case STANDARD:
                    binding.toneMixerResetMusicBtn.setEnabled(true);
                    binding.toneMixerResetMusicBtn.setBackgroundResource(R.drawable.background_btn_standard);
                    break;
                case INACTIVE:
                    binding.toneMixerResetMusicBtn.setEnabled(false);
                    binding.toneMixerResetMusicBtn.setBackgroundResource(R.drawable.background_btn_inactive);
            }
        }
    }

    private int convertDurationInSecondsToWidthInPx(double duration) {
        int oneSecondWidthPixels = getOneSecondWidthPixels();
        return (int) Math.round(oneSecondWidthPixels * duration);
    }

    private double convertWidthInPxToDurationInSeconds(int widthInPx) {
        int oneSecondWidthPixels = getOneSecondWidthPixels();
        return (double) widthInPx / oneSecondWidthPixels;
    }

    private int getOneSecondWidthPixels() {
        return 2 * getResources().getDimensionPixelSize(R.dimen.tone_mixer_scale_label_width);    // 1 scale_label_width = 0.5s
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

        builder.setMessage(R.string.alert_dialog_tone_mixer_exit_message);
        builder.setPositiveButton(R.string.alert_dialog_tone_mixer_exit_positive, (dialog, which) -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.changeFragmentFromToneMixer(fragmentId);
        });
        builder.setNegativeButton(R.string.alert_dialog_tone_mixer_exit_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}