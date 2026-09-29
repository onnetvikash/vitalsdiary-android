package in.devexis.vitalsdiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.devexis.vitalsdiary.core.CaseLimitPolicy;
import in.devexis.vitalsdiary.core.HealthCase;
import in.devexis.vitalsdiary.data.PremiumManager;
import in.devexis.vitalsdiary.data.VitalsRepository;

public class MainActivity extends AppCompatActivity implements CaseAdapter.OnCaseClickListener {

    private VitalsRepository repository;
    private PremiumManager premiumManager;
    private CaseAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        repository = new VitalsRepository(this);
        premiumManager = new PremiumManager(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerCases);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CaseAdapter(this);
        recyclerView.setAdapter(adapter);

        emptyText = findViewById(R.id.emptyText);

        findViewById(R.id.fabAddCase).setOnClickListener(v -> onAddCaseClicked());

        if (!premiumManager.hasSeenDisclaimer()) {
            showDisclaimerDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<HealthCase> cases = repository.getAllCases();
        adapter.submitList(cases);
        emptyText.setVisibility(cases.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDisclaimerDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_body)
                .setCancelable(false)
                .setPositiveButton(R.string.disclaimer_accept, (dialog, which) -> premiumManager.setDisclaimerSeen())
                .show();
    }

    private void onAddCaseClicked() {
        int currentCount = repository.countCases();
        if (!CaseLimitPolicy.canCreateNewCase(currentCount, premiumManager.isPremium())) {
            showUpgradeDialog();
            return;
        }
        showNewCaseDialog();
    }

    private void showNewCaseDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.new_case_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        new AlertDialog.Builder(this)
                .setTitle(R.string.new_case_title)
                .setView(input)
                .setPositiveButton(R.string.action_create, (dialog, which) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        repository.createCase(title);
                        refreshList();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showUpgradeDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.upgrade_title)
                .setMessage(getString(R.string.upgrade_body, CaseLimitPolicy.FREE_CASE_LIMIT))
                .setPositiveButton(R.string.upgrade_action, (dialog, which) ->
                        startActivity(new Intent(this, SettingsActivity.class)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onCaseClick(HealthCase healthCase) {
        Intent intent = new Intent(this, CaseDetailActivity.class);
        intent.putExtra(CaseDetailActivity.EXTRA_CASE_ID, healthCase.id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
