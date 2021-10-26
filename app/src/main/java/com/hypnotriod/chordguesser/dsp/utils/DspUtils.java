package com.hypnotriod.chordguesser.dsp.utils;

/**
 * Created by Ilya Pikin on 25.10.2021.
 */

public class DspUtils {
    public static void suppressHarmonics(double[] frequencies, double[] peaks, double factor, double fade, int deep, double threshold) {
        double root;
        int d;
        double f;
//        mainLoop:
        for (int i = 0; i < frequencies.length; i++) {
            if (peaks[i] < threshold) {
                peaks[i] = 0;
                continue;
            }
            /*
            root = frequencies[i] * 2;
            for (int j = i + 1; j < frequencies.length; j++) {
                if (Math.round(frequencies[j] / 4) == Math.round(root / 4)) {
                    if (peaks[i] < peaks[j]) {
                        peaks[i] -= peaks[j] * factor;
                        if (peaks[i] < threshold) {
                            peaks[i] = 0;
                        }
                        continue mainLoop;
                    }
                    break;
                }
            }
            */
            for (int j = i + 1; j < frequencies.length; j++) {
                d = deep;
                f = factor;
                root = frequencies[i] * 2;
                while (d-- > 0) {
                    if (Math.round(frequencies[j] / 4) == Math.round(root / 4)) {
                        peaks[j] -= peaks[i] * f;
                        break;
                    }
                    root += frequencies[i];
                    f *= fade;
                }
            }
        }
    }

    public static void fillFundamental(double[] frequenciesFundamental, double[] peaksFundamental, double[] frequencies, double[] peaks) {
        int cnt = 0;
        int maxAt;
        while (cnt < frequenciesFundamental.length) {
            maxAt = 0;
            for (int i = 0; i < peaks.length; i++) {
                if (peaks[i] > peaks[maxAt]) {
                    maxAt = i;
                }
            }
            if (peaks[maxAt] == 0) break;
            frequenciesFundamental[cnt] = frequencies[maxAt];
            peaksFundamental[cnt] = peaks[maxAt];
            peaks[maxAt] = 0;
            cnt++;
        }
    }

    public static void selectionSort(double[] frequencies, double[] peaks) {
        int minAt;
        double temp;
        for (int i = 0; i < frequencies.length; i++) {
            minAt = i;
            for (int j = i + 1; j < frequencies.length; j++) {
                if (frequencies[minAt] > frequencies[j]) {
                    minAt = j;
                }
            }
            if (minAt == i) continue;
            temp = peaks[minAt];
            peaks[minAt] = peaks[i];
            peaks[i] = temp;
            temp = frequencies[minAt];
            frequencies[minAt] = frequencies[i];
            frequencies[i] = temp;
        }
    }
}
