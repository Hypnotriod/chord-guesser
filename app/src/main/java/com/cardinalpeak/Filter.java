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

    int m_num_taps;
    int m_chunk_size;
    double m_Fs;
    double m_Fx;
    double m_Fu;
    double m_phi;
    double m_lambda;
    double[] m_taps;
    double[] m_sr;

    public Filter(FilterType type, int num_taps, int chunk_size, double Fs, double Fx) {
        m_num_taps = num_taps;
        m_chunk_size = chunk_size;
        m_Fs = Fs;
        m_Fx = Fx;
        m_lambda = Math.PI * Fx / (Fs / 2);

        if (Fs <= 0) throw new Error("Sample rate must not be less than or equal to 0");
        if (Fx <= 0 || Fx >= Fs / 2)
            throw new Error("Frequency must not be less than or equal to 0 and less than Nyquist frequency");
        if (m_num_taps <= 0) throw new Error("Taps number must not be less than or equal to 0");

        m_taps = new double[m_num_taps];
        m_sr = new double[m_num_taps + m_chunk_size];

        init();

        if (type == FilterType.LPF) designLPF();
        else if (type == FilterType.HPF) designHPF();
        else throw new Error("Only LPF or HPF types are supported");
    }

    public Filter(FilterType type, int num_taps, int chunk_size, double Fs, double Fl, double Fu) {
        m_num_taps = num_taps;
        m_chunk_size = chunk_size;
        m_Fs = Fs;
        m_Fx = Fl;
        m_Fu = Fu;
        m_lambda = Math.PI * Fl / (Fs / 2);
        m_phi = Math.PI * Fu / (Fs / 2);

        if (Fs <= 0) throw new Error("Sample rate must not be less than or equal to 0");
        if (Fl <= 0 || Fl >= Fs / 2)
            throw new Error("Bottom frequency must not be less than or equal to 0 and less than Nyquist frequency");
        if (Fu <= 0 || Fu >= Fs / 2)
            throw new Error("Top frequency must not be less than or equal to 0 and less than Nyquist frequency");
        if (m_num_taps <= 0) throw new Error("Taps number must not be less than or equal to 0");

        m_taps = new double[m_num_taps];
        m_sr = new double[m_num_taps + m_chunk_size - 1];

        init();

        if (type == FilterType.BPF) designBPF();
        else throw new Error("Only BPF type is supported");
    }

    public void process(double[] data) {
        double result;
        for (int i = m_chunk_size - 1, j = 0; i >= 0; i--, j++) {
            m_sr[i] = data[j];
            result = 0;
            for (int k = 0; k < m_num_taps; k++) {
                result += m_sr[i + k] * m_taps[k];
            }
            data[j] = result;
        }
        System.arraycopy(m_sr, 0, m_sr, m_chunk_size, m_sr.length - m_chunk_size);
    }

    public void extractTaps(double[] taps) {
        System.arraycopy(m_taps, 0, taps, 0, m_num_taps);
    }

    protected void init() {
        Arrays.fill(m_sr, 0);
    }

    protected void designLPF() {
        double mm;

        for (int n = 0; n < m_num_taps; n++) {
            mm = n - (m_num_taps - 1.0) / 2.0;
            if (mm == 0.0) m_taps[n] = m_lambda / Math.PI;
            else m_taps[n] = Math.sin(mm * m_lambda) / (mm * Math.PI);
        }
    }

    protected void designHPF() {
        double mm;

        for (int n = 0; n < m_num_taps; n++) {
            mm = n - (m_num_taps - 1.0) / 2.0;
            if (mm == 0.0) m_taps[n] = 1.0 - m_lambda / Math.PI;
            else m_taps[n] = -Math.sin(mm * m_lambda) / (mm * Math.PI);
        }
    }

    protected void designBPF() {
        double mm;

        for (int n = 0; n < m_num_taps; n++) {
            mm = n - (m_num_taps - 1.0) / 2.0;
            if (mm == 0.0) m_taps[n] = (m_phi - m_lambda) / Math.PI;
            else m_taps[n] = (Math.sin(mm * m_phi) -
                    Math.sin(mm * m_lambda)) / (mm * Math.PI);
        }
    }
}
