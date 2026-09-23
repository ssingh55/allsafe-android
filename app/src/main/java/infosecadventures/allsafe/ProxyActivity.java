package infosecadventures.allsafe;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public class ProxyActivity extends AppCompatActivity {

    // Define a hardcoded allowlist of trusted internal components
    // Replace with your application's legitimate internal components
    private static final Set<ComponentName> ALLOWED_INTERNAL_COMPONENTS = new HashSet<>();
    static {
        // Example: ALLOWED_INTERNAL_COMPONENTS.add(new ComponentName("infosecadventures.allsafe", "infosecadventures.allsafe.TrustedInternalActivity"));
        // Add all legitimate internal activities that this ProxyActivity is allowed to launch.
        // If no internal activities should be launched this way, the set should remain empty.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //... other initialization code...

        Intent incomingIntent = getIntent();
        Intent nestedIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            nestedIntent = incomingIntent.getParcelableExtra("extra_intent", Intent.class);
        } else {
            // For older Android versions, use the deprecated method
            // Suppress the deprecation warning as it's necessary for backward compatibility
            @SuppressWarnings("deprecation")
            android.os.Parcelable parcelable = incomingIntent.getParcelableExtra("extra_intent");
            nestedIntent = (parcelable instanceof Intent) ? (Intent) parcelable: null;
        }

        if (nestedIntent != null) {
            ComponentName resolvedComponent = nestedIntent.resolveActivity(getPackageManager());

            if (resolvedComponent != null && ALLOWED_INTERNAL_COMPONENTS.contains(resolvedComponent)) {
                // Only start the activity if its ComponentName is in the allowlist
                startActivity(nestedIntent);
            } else {
                // Log the attempt to launch an unallowed component for monitoring
                // Optionally, display an error or redirect to a safe default activity
                // Log.w("ProxyActivity", "Attempted to launch unallowed component: " + (resolvedComponent != null ? resolvedComponent.flattenToShortString(): "null"));
            }
        }
    }
}