package com.hypnotriod.chordguesser.dsp.utils;

/**
 * Created by Ilya Pikin on 25.10.2021.
 */

public class DspUtils {
    public static void suppressHarmonics(double[] frequencies, double[] peaks, double factor, int deep, double threshold) {
        double[] peaksOut = new double[peaks.length];
        System.arraycopy(peaks, 0, peaksOut, 0, peaks.length);
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] == 0) break;
            for (int j = i + 1; j < frequencies.length; j++) {
                if (frequencies[j] == 0) break;
                double div = frequencies[j] / frequencies[i];
                if (div > deep || Math.abs(div * 4 - Math.round(div * 4)) > 0.033) {
                    continue;
                }
                peaksOut[j] -= peaks[i] * factor;
            }
        }
        for (int i = 0; i < peaks.length; i++) {
            peaks[i] = peaksOut[i] < peaks[0] ? 0 : peaksOut[i];
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
