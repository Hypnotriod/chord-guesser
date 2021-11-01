package com.hypnotriod.chordguesser;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hypnotriod.chordguesser.dsp.Dsp;
import com.hypnotriod.chordguesser.dsp.DspResult;
import com.hypnotriod.chordguesser.dsp.DspResultViewer;

public class MainActivity extends AppCompatActivity implements DspResultViewer {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    private Dsp dsp = new Dsp(this);

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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                dsp.startRecorder();
            else
                finish();
        }
    }

    public void update(DspResult dspResult) {
        this.runOnUiThread(() -> {
            txtFrequency.setText(TextUtils.join(" ", dspResult.notes));
            txtCents.setText(TextUtils.join(" ", dspResult.cents));
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);
    }


}