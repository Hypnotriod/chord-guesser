/*
 * FIR filter class, by Mike Perkins
 *
 * a simple C++ class for linear phase FIR filtering
 *
 * For background, see the post http://www.cardinalpeak.com/blog?p=1841
 *
 * Copyright (c) 2013, Cardinal Peak, LLC.  http://www.cardinalpeak.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1) Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3) Neither the name of Cardinal Peak nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * CARDINAL PEAK, LLC BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package com.cardinalpeak;

import java.util.Arrays;

public class Filter {
    public enum FilterType {
        LPF,
        HPF,
        BPF
    }

    int tapsNum;
    int chunkSize;
    double phi;
    double lambda;
    double[] taps;
    double[] buffer;

    public Filter(FilterType type, int tapsNum, int chunkSize, double sampleRate, double frequency) {
        this.tapsNum = tapsNum;
        this.chunkSize = chunkSize;
        lambda = Math.PI * frequency / (sampleRate / 2);

        if (sampleRate <= 0) throw new Error("Sample rate must not be less than or equal to 0");
        if (frequency <= 0 || frequency >= sampleRate / 2)
            throw new Error("Frequency must not be less than or equal to 0 and less than Nyquist frequency");
        if (tapsNum <= 0) throw new Error("Taps number must not be less than or equal to 0");
        if (chunkSize <= 0) throw new Error("Chunk size must not be less than or equal to 0");

        init();

        if (type == FilterType.LPF) designLPF();
        else if (type == FilterType.HPF) designHPF();
        else throw new Error("Only LPF or HPF types are supported");
    }

    public Filter(FilterType type, int tapsNum, int chunkSize, double sampleRate, double frequencyBottom, double frequencyTop) {
        this.tapsNum = tapsNum;
        this.chunkSize = chunkSize;
        lambda = Math.PI * frequencyBottom / (sampleRate / 2);
        phi = Math.PI * frequencyTop / (sampleRate / 2);

        if (sampleRate <= 0) throw new Error("Sample rate must not be less than or equal to 0");
        if (frequencyBottom <= 0 || frequencyBottom >= sampleRate / 2)
            throw new Error("Bottom frequency must not be less than or equal to 0 and less than Nyquist frequency");
        if (frequencyTop <= 0 || frequencyTop >= sampleRate / 2)
            throw new Error("Top frequency must not be less than or equal to 0 and less than Nyquist frequency");
        if (tapsNum <= 0) throw new Error("Taps number must not be less than or equal to 0");
        if (chunkSize <= 0) throw new Error("Chunk size must not be less than or equal to 0");

        init();

        if (type == FilterType.BPF) designBPF();
        else throw new Error("Only BPF type is supported");
    }

    public void process(double[] data) {
        double result;
        for (int i = chunkSize - 1, j = 0; i >= 0; i--, j++) {
            buffer[i] = data[j];
            result = 0;
            for (int k = 0; k < tapsNum; k++) {
                result += buffer[i + k] * taps[k];
            }
            data[j] = result;
        }
        System.arraycopy(buffer, 0, buffer, chunkSize, buffer.length - chunkSize);
    }

    public void extractTaps(double[] taps) {
        System.arraycopy(this.taps, 0, taps, 0, tapsNum);
    }

    protected void init() {
        taps = new double[tapsNum];
        buffer = new double[tapsNum + chunkSize - 1];
        Arrays.fill(buffer, 0);
    }

    protected void designLPF() {
        double mm;

        for (int n = 0; n < tapsNum; n++) {
            mm = n - (tapsNum - 1.0) / 2.0;
            if (mm == 0.0) taps[n] = lambda / Math.PI;
            else taps[n] = Math.sin(mm * lambda) / (mm * Math.PI);
        }
    }

    protected void designHPF() {
        double mm;

        for (int n = 0; n < tapsNum; n++) {
            mm = n - (tapsNum - 1.0) / 2.0;
            if (mm == 0.0) taps[n] = 1.0 - lambda / Math.PI;
            else taps[n] = -Math.sin(mm * lambda) / (mm * Math.PI);
        }
    }

    protected void designBPF() {
        double mm;

        for (int n = 0; n < tapsNum; n++) {
            mm = n - (tapsNum - 1.0) / 2.0;
            if (mm == 0.0) taps[n] = (phi - lambda) / Math.PI;
            else taps[n] = (Math.sin(mm * phi) -
                    Math.sin(mm * lambda)) / (mm * Math.PI);
        }
    }
}
