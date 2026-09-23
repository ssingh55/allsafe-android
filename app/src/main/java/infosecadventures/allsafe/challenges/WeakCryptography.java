package infosecadventures.allsafe.challenges;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
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
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import infosecadventures.allsafe.R;
import infosecadventures.allsafe.utils.SnackUtil;

public class WeakCryptography extends Fragment {

    public static final String KEY = "1nf053c4dv3n7ur3";

    public static String encrypt(String value) {
        try {
            return SecureCryptoManager.encryptData(value);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
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
        int bound = 100000;
        int randomNumber = secureRandom.nextInt(bound);
        return Integer.toString(randomNumber + 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weak_cryptography, container, false);
        final EditText secret = view.findViewById(R.id.secret);
        view.findViewById(R.id.encrypt).setOnClickListener(v -> {
            String plain_text = secret.getText().toString();
            if (!plain_text.isEmpty()) {
                SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Result: " + encrypt(plain_text));
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

class SecureCryptoManager {
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALIAS = "secure_app_key";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits

    private SecureCryptoManager() { /* Private constructor to prevent instantiation */ }

    public static SecretKey getOrCreateSecretKey() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            return keyGenerator.generateKey();
        } else {
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            return secretKeyEntry.getSecretKey();
        }
    }

    public static String encryptData(String plaintext) throws GeneralSecurityException, IOException {
        SecretKey key = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    public static String decryptData(String encryptedData) throws GeneralSecurityException, IOException {
        SecretKey key = getOrCreateSecretKey();
        byte[] combined = Base64.decode(encryptedData, Base64.NO_WRAP);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}