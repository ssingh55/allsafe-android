package infosecadventures.allsafe.challenges;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import infosecadventures.allsafe.R;
import infosecadventures.allsafe.utils.SnackUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CertificatePinning extends Fragment {

    private static final String PRIMARY_PIN = "sha256/5kJvNEMw+2+mlBiGTPUpBGyT68=";
    private static final String BACKUP_PIN = "sha256/r/mIkG3eDigkrVA50Bj+cVvXzhMB5OUlY10hMYvfD5s=";
    private final List<String> hashes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_certificate_pinning, container, false);
        setHasOptionsMenu(true);

        //  make an intentional request with broken config
        //  to get the actual peer certificate chain public key hashes from  okhttp exception
        extractPeerCertificateChain();

        Button test = view.findViewById(R.id.execute);
        test.setOnClickListener(v -> {

            CertificatePinner.Builder certificatePinner = new CertificatePinner.Builder();
            for (String hash : hashes) {
                Log.d("ALLSAFE", "Processing certificate hash.");
                certificatePinner.add("httpbin.io", hash);
            }

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .certificatePinner(certificatePinner.build())
                    .build();

            Request request = new Request.Builder()
                    .url("https://httpbin.io/json")
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    final String message = e.getMessage();
                    Log.d("ALLSAFE", "Network request failed during certificate pinning.");
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> SnackUtil.INSTANCE.simpleMessage(requireActivity(), message != null ? message : "Connection failed!"));
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Successful connection over HTTPS!");
                            }
                        });
                    }
                }
            });
        });
        return view;
    }

    private void extractPeerCertificateChain() {
        CertificatePinner.Builder certificatePinnerBuilder = new CertificatePinner.Builder();
        certificatePinnerBuilder.add("httpbin.io", PRIMARY_PIN);
        certificatePinnerBuilder.add("httpbin.io", BACKUP_PIN);
        
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .certificatePinner(certificatePinnerBuilder.build())
                .build();

        Request request = new Request.Builder()
                .url("https://httpbin.io/json")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        String message = e.getMessage();
                        Log.d("ALLSAFE", "Certificate pinning validation failed: " + message);
                    });
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                // Certificate pinning validation successful
            }
        });
    }
}
