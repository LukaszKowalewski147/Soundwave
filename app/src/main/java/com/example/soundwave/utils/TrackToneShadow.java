package com.example.soundwave.utils;

import android.view.DragEvent;
import android.view.View;

public class TrackToneShadow {
    private final View view;
    private final int leftEdge;
    private final int rightEdge;
    private final int width;
    private final int trackPaddingStart;

    public TrackToneShadow(DragEvent event, int trackPaddingStart) {
        this.view = (View) event.getLocalState();
        int middle = (int) event.getX();
        this.width = view.getWidth();
        this.leftEdge = Math.max((int) Math.round(middle - width / 2.0d), 0);   // 0 if leftEdge is < 0
        this.rightEdge = leftEdge + width;
        this.trackPaddingStart = trackPaddingStart;
    }

    public View getView() {
        return view;
    }

    public int getLeftEdge() {
        return leftEdge;
    }

    public int getRightEdge() {
        return rightEdge;
    }

    public int getWidth() {
        return width;
    }

    public int getTrackPaddingStart() {
        return trackPaddingStart;
    }
}
