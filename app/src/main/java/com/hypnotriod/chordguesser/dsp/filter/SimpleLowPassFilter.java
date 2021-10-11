package com.hypnotriod.chordguesser.dsp.filter;

public class SimpleLowPassFilter {
    double coefficient;
    double value = 0;

    public SimpleLowPassFilter(double coefficient) {
        if (coefficient < 0 || coefficient > 1)
            throw new Error("Coefficient must not be less than 0 or more than 1");
        this.coefficient = 1 - coefficient;
    }

    public void process(double[] data) {
        for (int i = 0; i < data.length; i++) {
            value += (data[i] - value) * coefficient;
            data[i] = value;
        }
    }
}
