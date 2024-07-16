package com.example.soundwave.utils;

public class TrackToneData {
    private final int leftEdge;
    private final int rightEdge;
    private final int width;

    public TrackToneData(int width, int middle) {
        this.width = width;
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
