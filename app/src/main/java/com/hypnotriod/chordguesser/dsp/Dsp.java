package com.hypnotriod.chordguesser.dsp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.cardinalpeak.Filter;
import com.hypnotriod.chordguesser.dsp.filter.HannWindow;
import com.hypnotriod.chordguesser.dsp.utils.FftUtils;
import com.hypnotriod.chordguesser.dsp.utils.NotesUtil;
import com.hypnotriod.chordguesser.dsp.utils.PcmConvertUtil;
import com.meapsoft.Fft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Dsp {
    public static final int SAMPLE_RATE = 22050;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MIN_INTERNAL_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    public static final int BUFFER_SIZE = Math.max(MIN_INTERNAL_BUFFER_SIZE, 2048);
    public static final int TAPS_NUM = BUFFER_SIZE / 2;
    public static final double THRESHOLD = 0.1;
    public static final int FREQUENCIES_MAX = 4;
    public static final int MOVING_AVERAGE_FREQUENCIES_MAX = 8;
    public static final int MOVING_AVERAGE_WINDOW_START = (MOVING_AVERAGE_FREQUENCIES_MAX / 4);
    public static final int MOVING_AVERAGE_WINDOW_END = (MOVING_AVERAGE_FREQUENCIES_MAX / 4) * 3;
    public static final int MOVING_AVERAGE_WINDOW_SIZE = (MOVING_AVERAGE_FREQUENCIES_MAX / 2);
    public static final int BAND_PASS_TAPS_NUM = 51;
    public static final double BAND_PASS_TOP = 3000;
    public static final double BAND_PASS_BOTTOM = 100;

    private final DspResultViewer resultViewer;
    private final DspResult dspResult = new DspResult();

    private AudioRecord audioRecord;
    private final Fft fft = new Fft(TAPS_NUM);
    private final Filter bpFilter = new Filter(Filter.FilterType.BPF, BAND_PASS_TAPS_NUM, SAMPLE_RATE, BAND_PASS_BOTTOM, BAND_PASS_TOP);
    private final List<Queue<Double>> freqBuffList = new ArrayList<>();
    private final HannWindow hannWindow = new HannWindow(TAPS_NUM);

    public Dsp(DspResultViewer resultViewer) {
        this.resultViewer = resultViewer;
        for (int i = 0; i < FREQUENCIES_MAX; i++) {
            freqBuffList.add(new LinkedList<>());
        }
        Arrays.fill(dspResult.frequencies, "");
        Arrays.fill(dspResult.cents, "");
    }

    public void startRecorder() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        audioRecord.startRecording();
        runReader();
    }

    private void runReader() {
        new Thread(() -> {
            byte[] buff = new byte[BUFFER_SIZE];
            while (audioRecord != null) {
                audioRecord.read(buff, 0, buff.length);
                processDsp(buff);
            }
        }).start();
    }

    private void processDsp(byte[] data) {
        double[] real = PcmConvertUtil.convert16BitMono(data);
        double[] imaginary = new double[TAPS_NUM];
        double[] pow = new double[TAPS_NUM];
        double[] normPow = new double[TAPS_NUM];

        bpFilter.process(real);
        hannWindow.process(real);
        fft.fft(real, imaginary);
        FftUtils.fillPow(real, imaginary, pow);
        FftUtils.fillNormalizedPow(pow, normPow);

        double[] frequencies = new double[FREQUENCIES_MAX];
        double[] peaks = new double[FREQUENCIES_MAX];

        FftUtils.fillFrequencies(pow, normPow, frequencies, peaks, SAMPLE_RATE, THRESHOLD);
        processNextFrequencies(frequencies, peaks);
        resultViewer.update(dspResult);
    }

    private void processNextFrequencies(double[] frequencies, double[] peaks) {
        for (int i = 0; i < frequencies.length; i++) {
            processNextFrequency(frequencies[i], peaks[i], i);
        }
    }

    private void processNextFrequency(double frequency, double peak, int index) {
        double averageFrequency = 0;
        List<Double> movingAverageFreqWindow;
        Queue<Double> freqBuff = freqBuffList.get(index);
        if (frequency == 0) {
            freqBuff.poll();
            if (freqBuff.size() == 0) {
                dspResult.frequencies[index] = "";
                dspResult.cents[index] = "";
            }
            return;
        }
        freqBuff.add(frequency);
        if (freqBuff.size() < MOVING_AVERAGE_FREQUENCIES_MAX) return;
        freqBuff.poll();

        movingAverageFreqWindow = new ArrayList<>(freqBuff);
        Collections.sort(movingAverageFreqWindow);

        for (int i = MOVING_AVERAGE_WINDOW_START; i < MOVING_AVERAGE_WINDOW_END; i++) {
            averageFrequency += movingAverageFreqWindow.get(i);
        }
        averageFrequency /= MOVING_AVERAGE_WINDOW_SIZE;

        dspResult.frequencies[index] = NotesUtil.getNoteByFrequency(averageFrequency);
        dspResult.cents[index] = NotesUtil.getNoteCentsByFrequency(averageFrequency);
    }
}
