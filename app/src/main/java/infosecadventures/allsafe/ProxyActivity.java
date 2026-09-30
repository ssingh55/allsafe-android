package infosecadventures.allsafe;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public class ProxyActivity extends AppCompatActivity {

    // Define an allowlist of trusted ComponentNames that this activity is permitted to forward to.
    // Replace "com.example.app.TrustedActivity" with actual trusted internal components.
    private static final Set<ComponentName> ALLOWED_COMPONENTS = new HashSet<>();

    static {
        // Example: Add your trusted internal activities here.
        // ALLOWED_COMPONENTS.add(new ComponentName("infosecadventures.allsafe", "infosecadventures.allsafe.TrustedInternalActivity"));
        // ALLOWED_COMPONENTS.add(new ComponentName("infosecadventures.allsafe", "infosecadventures.allsafe.AnotherTrustedActivity"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent incomingIntent = getIntent();
        Intent forwardIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            forwardIntent = incomingIntent.getParcelableExtra("extra_intent", Intent.class);
        } else {
            // For older Android versions, use the deprecated method.
            // Suppress the deprecation warning as it's necessary for backward compatibility.
            @SuppressWarnings("deprecation")
            Parcelable parcelableExtra = incomingIntent.getParcelableExtra("extra_intent");
            forwardIntent = (Intent) parcelableExtra;
        }

        if (forwardIntent != null) {
            ComponentName resolvedComponent = forwardIntent.resolveActivity(getPackageManager());

            if (resolvedComponent != null && ALLOWED_COMPONENTS.contains(resolvedComponent)) {
                // Only forward if the resolved component is in the allowlist
                startActivity(forwardIntent);
            } else {
                // Log this event for anomaly detection, but do not forward the intent.
                // For example: Log.w("ProxyActivity", "Attempted to forward to an untrusted component: " + resolvedComponent);
            }
        }
    }
}