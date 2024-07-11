package com.example.soundwave.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.soundwave.R;
import com.example.soundwave.components.MixerComponent;
import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.additionalviews.OnToneSelectedListener;
import com.example.soundwave.additionalviews.SelectToneToMixDialogFragment;
import com.example.soundwave.databinding.FragmentToneMixerBinding;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneMixerViewModel;

import java.util.List;
import java.util.Locale;

public class ToneMixerFragment extends Fragment implements OnToneSelectedListener {

    private ToneMixerViewModel viewModel;
    private FragmentToneMixerBinding binding;

    private View.OnTouchListener mainTouchListener;
    private View.OnDragListener trackToneDragListener;
    private View.OnDragListener trackToneRemoveDragListener;
    private View.OnLongClickListener toneWorkbenchLongClickListener;
    private View.OnLongClickListener toneTrackLongClickListener;
    private View.OnClickListener toneClickListener;
    private View.OnClickListener toneRemoveClickListener;

    private ValueAnimator track1animator;
    private ValueAnimator track2animator;
    private ValueAnimator track3animator;
    private ValueAnimator track4animator;
    private ValueAnimator track5animator;

    private View toneToRemove;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f);
            label.setText(String.format(Locale.US, "|%.1fs", i/2.0f));   // i/2.0f = 0.5s every R.dimen.tone_mixer_scale_label_width

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
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();
                    LinearLayout track;
                    boolean toneDroppable;

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

                    stopPreviousTrackAnimation(track);
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
                    draggedView = (View) event.getLocalState();
                    owner = (ViewGroup) draggedView.getParent();
                    track = (LinearLayout) v;

                    if (isToneDroppable(event, track)) {
                        if (owner != null)
                            owner.removeView(draggedView);

                        track.addView(draggedView, calculateDropIndex((LinearLayout) v, (int) event.getX()));
                        draggedView.setVisibility(View.VISIBLE);
                    }
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
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    int enteredColor = ContextCompat.getColor(requireContext(), R.color.delete_bin);
                    binding.toneMixerRemoveTrackToneIcon.setColorFilter(enteredColor);
                    binding.toneMixerRemoveTrackTone.setBackgroundResource(R.drawable.background_mixer_track_tone_remove_active);
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    int exitedColor = ContextCompat.getColor(requireContext(), R.color.gray);
                    binding.toneMixerRemoveTrackToneIcon.setColorFilter(exitedColor);
                    binding.toneMixerRemoveTrackTone.setBackgroundResource(R.drawable.background_mixer_track_tone_remove_inactive);
                    return true;
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();

                    if (owner != null)
                        owner.removeView(draggedView);

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
        viewModel.getMusic().observe(getViewLifecycleOwner(), new Observer<Music>() {
            @Override
            public void onChanged(Music music) {

            }
        });
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
            viewModel.generateMusic(getMixerComponent());
        });

        binding.toneMixerPlayStopMusicBtn.setOnClickListener(v -> {
            viewModel.playStopMusic();
        });
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

    private MixerComponent getMixerComponent() {
        List<Tone> track1Tones = viewModel.getTonesFromTrack(binding.toneMixerTrack1);
        List<Tone> track2Tones = viewModel.getTonesFromTrack(binding.toneMixerTrack2);
        List<Tone> track3Tones = viewModel.getTonesFromTrack(binding.toneMixerTrack3);
        List<Tone> track4Tones = viewModel.getTonesFromTrack(binding.toneMixerTrack4);
        List<Tone> track5Tones = viewModel.getTonesFromTrack(binding.toneMixerTrack5);

        return new MixerComponent(track1Tones, track2Tones, track3Tones, track4Tones, track5Tones);
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

        int oneSecondWidthPixels = 2 * getResources().getDimensionPixelSize(R.dimen.tone_mixer_scale_label_width);    // 1 scale_label_width = 0.5s
        int widthInPx = (int) Math.round(oneSecondWidthPixels * tone.getDurationInSeconds());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                widthInPx,
                (int) getResources().getDimension(R.dimen.tone_mixer_track_tone_height)
        );
        trackTone.setLayoutParams(layoutParams);

        // Measure and layout the view to ensure it has the correct dimensions to drag from workbench
        int widthSpec = View.MeasureSpec.makeMeasureSpec(widthInPx, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec((int) getResources().getDimension(R.dimen.tone_mixer_track_tone_height), View.MeasureSpec.EXACTLY);
        trackTone.measure(widthSpec, heightSpec);
        trackTone.layout(0, 0, trackTone.getMeasuredWidth(), trackTone.getMeasuredHeight());

        trackTone.setVisibility(View.INVISIBLE);

        return trackTone;
    }

    private boolean isToneDroppable(DragEvent event, LinearLayout track) {
        View tone = (View) event.getLocalState();
        int middleX = (int) event.getX();
        int width = tone.getWidth();
        int leftEdge = (int) Math.round(middleX - width / 2.0d);
        int rightEdge = leftEdge + width;

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

    private int calculateDropIndex(LinearLayout parent, int dropX) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            int childLeft = child.getLeft();
            int childRight = child.getRight();

            if (dropX < childLeft + child.getWidth() / 2) {
                return i;                   // Drop item before child
            } else if (dropX > childRight - child.getWidth() / 2) {
                continue;                   // Check next child
            } else {
                return i + 1;               // Drop item after child
            }
        }
        return parent.getChildCount();      // Drop item at the end
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

        stopPreviousTrackAnimation(track);
        setCurrentTrackAnimation(track, colorAnimation);

        return colorAnimation;
    }

    private void stopPreviousTrackAnimation(LinearLayout track) {
        int trackNumber = (int) track.getTag(R.id.tag_track_number);

        switch (trackNumber) {
            case 1:
                if (track1animator != null && track1animator.isRunning())
                    track1animator.cancel();
                break;
            case 2:
                if (track2animator != null && track2animator.isRunning())
                    track2animator.cancel();
                break;
            case 3:
                if (track3animator != null && track3animator.isRunning())
                    track3animator.cancel();
                break;
            case 4:
                if (track4animator != null && track4animator.isRunning())
                    track4animator.cancel();
                break;
            case 5:
                if (track5animator != null && track5animator.isRunning())
                    track5animator.cancel();
                break;
        }
    }

    private void setCurrentTrackAnimation(LinearLayout track, ValueAnimator animator) {
        int trackNumber = (int) track.getTag(R.id.tag_track_number);

        switch (trackNumber) {
            case 1:
                track1animator = animator;
                break;
            case 2:
                track2animator = animator;
                break;
            case 3:
                track3animator = animator;
                break;
            case 4:
                track4animator = animator;
                break;
            case 5:
                track5animator = animator;
                break;
        }
    }
}