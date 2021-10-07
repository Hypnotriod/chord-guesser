package com.hypnotriod.chordguesser;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hypnotriod.utils.FftUtils;
import com.hypnotriod.utils.NotesUtil;
import com.hypnotriod.utils.PcmConvertUtil;
import com.hypnotriod.utils.SimpleLowPassFilter;
import com.meapsoft.Fft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MIN_INTERNAL_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int BUFFER_SIZE = Math.max(MIN_INTERNAL_BUFFER_SIZE, 4096);
    private static final int SAMPLES_NUM = BUFFER_SIZE / 2;
    private static final double THRESHOLD = 0.1;
    private static final int FREQUENCIES_MAX = 4;
    private static final int AVERAGE_FREQUENCIES_MAX = 16;
    private static final int AVERAGE_WINDOW_START = (AVERAGE_FREQUENCIES_MAX / 4);
    private static final int AVERAGE_WINDOW_END = (AVERAGE_FREQUENCIES_MAX / 4) * 3;
    private static final int AVERAGE_WINDOW_SIZE = (AVERAGE_FREQUENCIES_MAX / 2);
    private static final double LOW_PASS_COEFFICIENT = 0.6;

    private AudioRecord audioRecord;
    private final Fft fft = new Fft(SAMPLES_NUM);
    private final SimpleLowPassFilter lpFilter = new SimpleLowPassFilter(LOW_PASS_COEFFICIENT);
    private final Queue<Double> averageFreqBuff = new LinkedList();

    private TextView txtFrequency;
    private TextView txtCents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtFrequency = findViewById(R.id.txtFrequency);
        txtCents = findViewById(R.id.txtCents);

        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startRecorder();
            else finish();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void startRecorder() {
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

    private void updateTextFieldAsync(TextView textField, String value) {
        this.runOnUiThread(() -> textField.setText(value));
    }

    private void processDsp(byte[] data) {
        double[] real = PcmConvertUtil.convert16BitMono(data);
        double[] imaginary = new double[SAMPLES_NUM];
        double[] pow = new double[SAMPLES_NUM];
        double[] normPow = new double[SAMPLES_NUM];
        double maxPeak = 0;
        double frequency = 0;

        lpFilter.process(real);
        FftUtils.hannWindow(real);
        fft.fft(real, imaginary);
        FftUtils.fillPow(real, imaginary, pow);
        FftUtils.fillNormalizedPow(pow, normPow);

        double[] frequencies = new double[FREQUENCIES_MAX];
        double[] peaks = new double[FREQUENCIES_MAX];

        FftUtils.fillFrequencies(pow, normPow, frequencies, peaks, SAMPLE_RATE, THRESHOLD);
        for (int i = 0; i < frequencies.length; i++) {
            if (maxPeak < peaks[i]) {
                maxPeak = peaks[i];
                frequency = frequencies[i];
            }
        }
        processNextFrequency(frequency);
    }

    private void processNextFrequency(double frequency) {
        double averageFrequency = 0;
        List<Double> averageFreqBuffSorted;
        if (frequency == 0) return;
        averageFreqBuff.add(frequency);
        if (averageFreqBuff.size() < AVERAGE_FREQUENCIES_MAX) return;
        averageFreqBuff.poll();

        averageFreqBuffSorted = new ArrayList<>(averageFreqBuff);
        Collections.sort(averageFreqBuffSorted);

        for (int i = AVERAGE_WINDOW_START; i < AVERAGE_WINDOW_END; i++) {
            averageFrequency += averageFreqBuffSorted.get(i);
        }
        averageFrequency /= AVERAGE_WINDOW_SIZE;

        updateTextFieldAsync(txtFrequency, NotesUtil.getNoteByFrequency(averageFrequency));
        updateTextFieldAsync(txtCents, NotesUtil.getNoteCentsByFrequency(averageFrequency));
    }
}