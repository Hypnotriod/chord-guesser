package com.hypnotriod.chordguesser.dsp;

import static com.hypnotriod.chordguesser.dsp.Dsp.FREQUENCIES_FUNDAMENTAL_NUM;

public class DspResult {
    public final String[] notes = new String[FREQUENCIES_FUNDAMENTAL_NUM];
    public final String[] cents = new String[FREQUENCIES_FUNDAMENTAL_NUM];
    public final double[] frequencies = new double[FREQUENCIES_FUNDAMENTAL_NUM];
}
