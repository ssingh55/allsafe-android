package infosecadventures.allsafe.challenges;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import infosecadventures.allsafe.R;
import infosecadventures.allsafe.utils.SecureKeyManager;
import infosecadventures.allsafe.utils.SnackUtil;

public class WeakCryptography extends Fragment {

    // Define a static nested class for EncryptionResult
    public static class EncryptionResult {
        public final byte[] ciphertext;
        public final byte[] iv;

        public EncryptionResult(byte[] ciphertext, byte[] iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
    }

    public static EncryptionResult encrypt(String plaintext, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[12]; // 96-bit IV for GCM
        secureRandom.nextBytes(iv);

        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit tag length (16 bytes * 8)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec); // Initialize with provided key and GCM spec

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        return new EncryptionResult(ciphertext, iv); // Return custom object containing ciphertext and IV
    }

    public static String md5Hash(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(text.getBytes());
            byte[] messageDigest = digest.digest();
            stringBuilder.append(String.format("%032X", new BigInteger(1, messageDigest)));
        } catch (Exception e) {
            Log.d("ALLSAFE", e.getLocalizedMessage());
        }
        return stringBuilder.toString();
    }

    public static String randomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        // Generate a random number between 0 (inclusive) and 100000 (exclusive),
        // then add 1 to match the original range (1 to 100000 inclusive).
        int n = secureRandom.nextInt(100000) + 1;
        return Integer.toString(n);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weak_cryptography, container, false);
        final EditText secret = view.findViewById(R.id.secret);
        view.findViewById(R.id.encrypt).setOnClickListener(v -> {
            String plain_text = secret.getText().toString();
            if (!plain_text.isEmpty()) {
                try {
                    SecretKey secureKey = SecureKeyManager.getOrCreateSecretKey();
                    EncryptionResult encryptedData = encrypt(plain_text, secureKey);
                    String base64Encoded = android.util.Base64.encodeToString(encryptedData.ciphertext, android.util.Base64.DEFAULT);
                    SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Result: " + base64Encoded);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                    SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Encryption failed");
                }
            } else {
                SnackUtil.INSTANCE.simpleMessage(requireActivity(), "First, you have to enter your secrets!");
            }
        });
        view.findViewById(R.id.hash).setOnClickListener(v -> {
            String plain_text = secret.getText().toString();
            if (!plain_text.isEmpty()) {
                SnackUtil.INSTANCE.simpleMessage(requireActivity(), "MD5 Hash: " + md5Hash(plain_text));
            } else {
                SnackUtil.INSTANCE.simpleMessage(requireActivity(), "First, you have to enter your secrets!");
            }
        });

        view.findViewById(R.id.random).setOnClickListener(v -> SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Random: " + randomNumber()));
        return view;
    }
}