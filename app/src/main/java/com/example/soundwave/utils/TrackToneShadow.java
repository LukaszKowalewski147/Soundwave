package com.example.soundwave.utils;

public class TrackToneShadow {
    private final int leftEdge;
    private final int rightEdge;

    public TrackToneShadow(int leftEdge, int rightEdge) {
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
    }

    public int getLeftEdge() {
        return leftEdge;
    }

    public int getRightEdge() {
        return rightEdge;
    }
}
