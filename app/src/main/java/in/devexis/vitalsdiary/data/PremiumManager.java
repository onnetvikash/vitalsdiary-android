package in.devexis.vitalsdiary.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracks the free/premium status and whether the medical disclaimer has been shown.
 * The premium flag here is a local demo toggle; a store release should set it from
 * the Google Play Billing Library's verified purchase state instead (see README).
 */
public class PremiumManager {
    private static final String PREFS = "vitals_prefs";
    private static final String KEY_PREMIUM = "is_premium";
    private static final String KEY_DISCLAIMER_SEEN = "disclaimer_seen";

    private final SharedPreferences prefs;

    public PremiumManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isPremium() {
        return prefs.getBoolean(KEY_PREMIUM, false);
    }

    public void setPremium(boolean premium) {
        prefs.edit().putBoolean(KEY_PREMIUM, premium).apply();
    }

    public boolean hasSeenDisclaimer() {
        return prefs.getBoolean(KEY_DISCLAIMER_SEEN, false);
    }

    public void setDisclaimerSeen() {
        prefs.edit().putBoolean(KEY_DISCLAIMER_SEEN, true).apply();
    }
}
