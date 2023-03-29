package com.hypnotriod.chordguesser.dsp.filter;

public class Normalizer {
    private final double attack;
    private final double release;
    private final double ampMax;
    private double amp = 1;
    private final double[] hold;

    public Normalizer(double attackTime, double releaseTime, double holdTime, double ampMax, int sampleRate) {
        this.attack = 1 / attackTime / (double) sampleRate;
        this.release = 1 / releaseTime / (double) sampleRate;
        this.hold = new double[(int) (sampleRate * holdTime)];
        this.ampMax = ampMax;
    }

    public void process(double[] data) {
        double hpeak = 0;
        for (double v : hold)
            hpeak = Math.max(v, hpeak);

        if (hold.length - data.length >= 0)
            System.arraycopy(hold, data.length, hold, 0, hold.length - data.length);

        for (int i = 0; i < data.length; i++) {
            data[i] *= amp;
            double peak = Math.abs(data[i]);
            int hi = Math.max(hold.length - data.length + i, i);
            if (hi < hold.length) hold[hi] = peak;
            hpeak = Math.max(peak, hpeak);
            if (hpeak < 1) {
                if (amp < ampMax)
                    amp += attack;
            } else {
                hpeak /= amp;
                amp -= release;
                hpeak *= amp;
            }
        }
    }
}
