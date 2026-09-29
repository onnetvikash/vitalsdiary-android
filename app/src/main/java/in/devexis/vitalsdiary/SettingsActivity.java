package in.devexis.vitalsdiary;

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import in.devexis.vitalsdiary.data.PremiumManager;

public class SettingsActivity extends AppCompatActivity {

    private PremiumManager premiumManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings_title);

        premiumManager = new PremiumManager(this);

        SwitchCompat premiumSwitch = findViewById(R.id.premiumSwitch);
        premiumSwitch.setChecked(premiumManager.isPremium());
        premiumSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                premiumManager.setPremium(isChecked));
    }
}
