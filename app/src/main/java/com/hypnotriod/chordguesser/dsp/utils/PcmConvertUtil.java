package com.hypnotriod.chordguesser.dsp.utils;

public class PcmConvertUtil {
    public static void convert16BitMono(byte[] input, double[] output) {
        int sample;
        for (int i = 0; i < output.length; i++) {
            sample = (short) (((input[i * 2 + 1] & 0xFF) << 8) | (input[i * 2] & 0xFF));
            output[i] = (double) sample / (double) Short.MAX_VALUE;
        }
    }
}
