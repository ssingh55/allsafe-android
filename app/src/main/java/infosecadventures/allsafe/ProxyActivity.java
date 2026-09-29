package infosecadventures.allsafe;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class ProxyActivity extends AppCompatActivity {

    // Define an allowlist of trusted ComponentNames that this activity is permitted to launch.
    // Replace "infosecadventures.allsafe.TrustedActivity" with actual trusted internal components.
    private static final Set<ComponentName> ALLOWED_COMPONENTS;
    static {
        ALLOWED_COMPONENTS = new HashSet<>();
        ALLOWED_COMPONENTS.add(new ComponentName("infosecadventures.allsafe", "infosecadventures.allsafe.TrustedActivity"));
        // Add other trusted ComponentName objects here if needed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent nestedIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            nestedIntent = getIntent().getParcelableExtra("extra_intent", Intent.class);
        } else {
            @SuppressWarnings("deprecation")
            Intent intent = (Intent) getIntent().getParcelableExtra("extra_intent");
            nestedIntent = intent;
        }
        
        if (nestedIntent != null) {
            ComponentName resolvedComponent = nestedIntent.resolveActivity(getPackageManager());
            if (resolvedComponent != null) {
                ComponentName componentName = new ComponentName(resolvedComponent.getPackageName(), resolvedComponent.getClassName());
                if (ALLOWED_COMPONENTS.contains(componentName)) {
                    startActivity(nestedIntent);
                } else {
                    // Log or handle the attempt to launch an unallowed component
                    // For security, silently discard or show an error to the user
                    // Log.w("ProxyActivity", "Attempted to launch unallowed component: " + componentName);
                }
            }
        }
    }
}