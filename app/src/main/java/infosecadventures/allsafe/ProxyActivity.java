package infosecadventures.allsafe;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class ProxyActivity extends AppCompatActivity {

    // Define an allowlist of trusted ComponentName objects
    // Replace with actual trusted components this app intends to forward to.
    private static final Set<ComponentName> ALLOWED = Set.of(
            // Example: new ComponentName("com.example.app", "com.example.app.TrustedTargetActivity")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent forward = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            forward = getIntent().getParcelableExtra("extra_intent", Intent.class);
        } else {
            // For older API levels, use the deprecated method
            // Suppress deprecation warning as it's necessary for backward compatibility
            @SuppressWarnings("deprecation")
            Intent deprecatedForward = getIntent().getParcelableExtra("extra_intent");
            forward = deprecatedForward;
        }

        if (forward != null) {
            ComponentName resolved = forward.resolveActivity(getPackageManager());
            if (resolved != null && ALLOWED.contains(resolved)) {
                startActivity(forward);
            } else {
                // Log this attempt for anomaly monitoring, but do not forward
                // Log.w("ProxyActivity", "Attempted to forward to unallowed component: " + (resolved != null ? resolved.flattenToShortString(): "null"));
            }
        }
    }
}