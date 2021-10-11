package com.hypnotriod.chordguesser.dsp.utils;

public class PcmConvertUtil {
    public static double[] convert16BitMono(byte[] data) {
        double[] result = new double[data.length / 2];
        int sample;
        for (int i = 0; i < result.length; i++) {
            sample = (short) (((data[i * 2 + 1] & 0xFF) << 8) | (data[i * 2] & 0xFF));
            result[i] = (double) sample / (double) Short.MAX_VALUE;
        }
        return result;
    }
}
