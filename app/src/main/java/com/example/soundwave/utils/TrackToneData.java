package com.example.soundwave.utils;

import android.view.DragEvent;
import android.view.View;

public class TrackToneData {
    private final int leftEdge;
    private final int rightEdge;
    private final int width;

    public TrackToneData(DragEvent event) {
        View view = (View) event.getLocalState();
        int middle = (int) event.getX();
        this.width = view.getWidth();
        this.leftEdge = Math.max((int) Math.round(middle - width / 2.0d), 0);   // 0 if leftEdge is < 0
        this.rightEdge = leftEdge + width;
    }

    public TrackToneData(int width, int leftEdge, int rightEdge) {
        this.width = width;
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
    }

    public int getLeft() {
        return leftEdge;
    }

    public int getRight() {
        return rightEdge;
    }

    public int getWidth() {
        return width;
    }
}
