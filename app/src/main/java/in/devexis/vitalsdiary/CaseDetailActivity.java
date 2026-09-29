package in.devexis.vitalsdiary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;

import in.devexis.vitalsdiary.core.HealthCase;
import in.devexis.vitalsdiary.core.TrendAnalyzer;
import in.devexis.vitalsdiary.data.VitalsRepository;

public class CaseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "case_id";

    private VitalsRepository repository;
    private long caseId;
    private TextView titleView;
    private TextView trendView;
    private EntryAdapter adapter;

    private final ActivityResultLauncher<Intent> addEntryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadCase());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);

        repository = new VitalsRepository(this);
        caseId = getIntent().getLongExtra(EXTRA_CASE_ID, -1);

        titleView = findViewById(R.id.detailTitle);
        trendView = findViewById(R.id.detailTrend);

        RecyclerView recyclerView = findViewById(R.id.recyclerEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntryAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fabAddEntry).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEntryActivity.class);
            intent.putExtra(AddEntryActivity.EXTRA_CASE_ID, caseId);
            addEntryLauncher.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCase();
    }

    private void loadCase() {
        HealthCase healthCase = repository.getCase(caseId);
        if (healthCase == null) {
            finish();
            return;
        }
        titleView.setText(healthCase.title);
        setTitle(healthCase.title);

        TrendAnalyzer.Trend trend = TrendAnalyzer.analyze(healthCase.entries());
        trendView.setText(getString(R.string.trend_format, trendLabel(trend)));

        adapter.submitList(healthCase.entries());
    }

    private String trendLabel(TrendAnalyzer.Trend trend) {
        switch (trend) {
            case IMPROVING:
                return getString(R.string.trend_improving);
            case WORSENING:
                return getString(R.string.trend_worsening);
            case STABLE:
                return getString(R.string.trend_stable);
            default:
                return getString(R.string.trend_insufficient);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_case_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_export) {
            exportPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportPdf() {
        HealthCase healthCase = repository.getCase(caseId);
        if (healthCase == null) {
            return;
        }
        try {
            File file = PdfExporter.export(this, healthCase);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.export_chooser_title)));
        } catch (IOException e) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
