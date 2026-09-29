package in.devexis.vitalsdiary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

import in.devexis.vitalsdiary.data.VitalsRepository;

public class AddEntryActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "case_id";

    private long caseId;
    private VitalsRepository repository;

    private SeekBar severitySeek;
    private TextView severityValue;
    private EditText noteInput;
    private ImageView photoPreview;
    private Button addPhotoBtn;
    private Button recordBtn;
    private Button playBtn;

    private File pendingPhotoFile;
    private String savedPhotoPath;
    private String savedAudioPath;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingPhotoFile != null) {
                    savedPhotoPath = pendingPhotoFile.getAbsolutePath();
                    photoPreview.setImageURI(Uri.fromFile(pendingPhotoFile));
                    photoPreview.setVisibility(View.VISIBLE);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestMicPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startRecording();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        setTitle(R.string.add_entry_title);

        caseId = getIntent().getLongExtra(EXTRA_CASE_ID, -1);
        repository = new VitalsRepository(this);

        severitySeek = findViewById(R.id.severitySeek);
        severityValue = findViewById(R.id.severityValue);
        noteInput = findViewById(R.id.noteInput);
        photoPreview = findViewById(R.id.photoPreview);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        recordBtn = findViewById(R.id.recordBtn);
        playBtn = findViewById(R.id.playBtn);

        severitySeek.setMax(9); // maps 0-9 to severity 1-10
        severitySeek.setProgress(4);
        updateSeverityLabel();
        severitySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeverityLabel();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        addPhotoBtn.setOnClickListener(v -> onAddPhotoClicked());
        recordBtn.setOnClickListener(v -> onRecordClicked());
        playBtn.setOnClickListener(v -> onPlayClicked());
        playBtn.setEnabled(false);

        findViewById(R.id.saveEntryBtn).setOnClickListener(v -> saveEntry());
    }

    private void updateSeverityLabel() {
        int severity = severitySeek.getProgress() + 1;
        severityValue.setText(getString(R.string.severity_format, severity));
    }

    private void onAddPhotoClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File dir = new File(getFilesDir(), "photos");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            pendingPhotoFile = new File(dir, "photo_" + System.currentTimeMillis() + ".jpg");
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pendingPhotoFile);
            takePicture.launch(uri);
        } catch (Exception e) {
            Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void onRecordClicked() {
        if (isRecording) {
            stopRecording();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startRecording() {
        try {
            File dir = new File(getFilesDir(), "audio");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File audioFile = new File(dir, "audio_" + System.currentTimeMillis() + ".m4a");
            savedAudioPath = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(savedAudioPath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordBtn.setText(R.string.stop_recording);
            playBtn.setEnabled(false);
        } catch (IOException e) {
            Toast.makeText(this, R.string.mic_unavailable, Toast.LENGTH_SHORT).show();
            savedAudioPath = null;
        }
    }

    private void stopRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (RuntimeException e) {
            savedAudioPath = null;
        }
        isRecording = false;
        recordBtn.setText(R.string.record_voice_note);
        playBtn.setEnabled(savedAudioPath != null);
    }

    private void onPlayClicked() {
        if (savedAudioPath == null) {
            return;
        }
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(savedAudioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveEntry() {
        int severity = severitySeek.getProgress() + 1;
        String note = noteInput.getText().toString().trim();
        repository.addEntry(caseId, severity, note, savedPhotoPath, savedAudioPath);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (RuntimeException ignored) {
                // already released or never started
            }
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
