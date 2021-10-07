package com.hypnotriod.utils;

public class SimpleLowPassFilter {
    double coefficient;
    double value = 0;

    public SimpleLowPassFilter(double coefficient) {
        this.coefficient = coefficient;
    }

    public void process(double[] data) {
        for (int i = 0; i < data.length; i++) {
            value += (data[i] - value) * coefficient;
            data[i] = value;
        }
    }
}
