package com.hypnotriod.chordguesser.dsp.filter;

public class HannWindow {
    double[] window;

    public HannWindow(int tapsNum) {
        buildWindow(tapsNum);
    }

    public void process(double[] data) {
        for (int i = 0; i < window.length; i++) {
            data[i] *= window[i];
        }
    }

    protected void buildWindow(int tapsNum) {
        window = new double[tapsNum];
        for (int i = 0; i < tapsNum; i++) {
            window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (tapsNum - 1)));
        }
    }
}
