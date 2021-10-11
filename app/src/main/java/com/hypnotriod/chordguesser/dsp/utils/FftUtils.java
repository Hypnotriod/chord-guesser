package com.hypnotriod.chordguesser.dsp.utils;

public class FftUtils {
    public static void fillPow(double[] real, double[] imaginary, double[] pow) {
        int i;
        for (i = 0; i < real.length; i++) {
            pow[i] = (real[i] * real[i] + imaginary[i] * imaginary[i]);
        }
    }

    public static void fillNormalizedPow(double[] pow, double[] normPow) {
        int i;
        double p = 1.0 / (Math.log(pow.length) / Math.log(2));
        for (i = 0; i < pow.length; i++) {
            normPow[i] = Math.sqrt(pow[i]) * p;
        }
    }

    public static void fillFrequencies(double[] pow, double[] normPow, double[] frequencies, double[] peaks, int sampleRate, double threshold) {
        int currIndex = 0;
        double fm = 0;
        double vm = 0;
        double vmn = 0;
        double v1;
        double v2;
        double v3;
        double vm1;
        double vm2;
        double vm3;
        double ncoef;
        double frequency;
        double peak;
        int i;

        for (i = 1; i <= pow.length / 2 && currIndex < frequencies.length; i++) {
            if (vm < pow[i]) {
                vm = pow[i];
                vmn = normPow[i];
                fm = i;
            } else if (vmn > threshold) {
                i--;
                v1 = pow[i - 1];
                v2 = i > 1 ? pow[i - 2] : 0.0;
                v3 = i > 2 ? pow[i - 3] : 0.0;
                vm1 = pow[i + 1];
                vm2 = pow[i + 2];
                vm3 = pow[i + 3];

                ncoef = vm / (vm + v1 + v2 + v3 + vm1 + vm2 + vm3);

                frequency = fm
                        - v1 / vm * ncoef
                        - v2 / vm * ncoef * 2.0
                        - v3 / vm * ncoef * 3.0
                        + vm1 / vm * ncoef
                        + vm2 / vm * ncoef * 2.0
                        + vm3 / vm * ncoef * 3.0;
                frequency *= ((double) sampleRate / (double) (pow.length / 2));

                peak = (1.0 - Math.cos((Math.PI / 2.0) * (frequency - fm))) * normPow[i] / 2.f + normPow[i];

                peaks[currIndex] = peak;
                frequencies[currIndex++] = frequency;

                vm = 0.f;
                vmn = 0.f;
                i += 4;
            }
        }
    }
}
