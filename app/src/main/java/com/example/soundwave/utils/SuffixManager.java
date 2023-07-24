package com.example.soundwave.utils;

public class SuffixManager {

    public static String addMillisecondSuffix(int value) {
        return value + "ms";
    }

    public static String addSecondSuffix(int value) {
        return value + "s";
    }

    public static String addHertzSuffix(int value) {
        return value + "Hz";
    }

    public static String addKiloHertzSuffix(int value) {
        return value + "kHz";
    }

    public static String addPercentSuffix(int value) {
        return value + "%";
    }

    public static String removeMillisecondSuffix(String value) {
        return value.replaceFirst("ms", "");
    }

    public static String removeSecondSuffix(String value) {
        return value.replaceFirst("s", "");
    }

    public static String removeHertzSuffix(String value) {
        return value.replaceFirst("Hz", "");
    }

    public static String removeKiloHertzSuffix(String value) {
        return value.replaceFirst("kHz", "");
    }

    public static String removePercentSuffix(String value) {
        return value.replaceFirst("%", "");
    }
}
