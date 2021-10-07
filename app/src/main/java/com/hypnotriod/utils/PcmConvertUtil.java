package com.hypnotriod.utils;

public class PcmConvertUtil {
    public static double[] convert16BitMono(byte[] data) {
        double[] result = new double[data.length / 2];
        int sample;
        for (int i = 0; i < result.length; i += 2) {
            sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
            result[i] = (double) sample / (double) Short.MAX_VALUE;
        }
        return result;
    }
}
