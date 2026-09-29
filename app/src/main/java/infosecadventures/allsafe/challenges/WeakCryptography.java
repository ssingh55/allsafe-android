package infosecadventures.allsafe.challenges;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import infosecadventures.allsafe.R;
import infosecadventures.allsafe.utils.SecureCryptoManager;
import infosecadventures.allsafe.utils.SnackUtil;

public class WeakCryptography extends Fragment {

    public static final String KEY = "1nf053c4dv3n7ur3";

    /**
     * @deprecated This method uses insecure AES/ECB/PKCS5PADDING with a hardcoded key.
     * Do not use. Migrate to a secure cryptographic implementation (e.g., AES/GCM)
     * and remove this method after migration is complete.
     */
    @Deprecated
    public static String encrypt(String value) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return new String(encrypted);
        } catch (
                NoSuchPaddingException |
                        NoSuchAlgorithmException |
                        InvalidKeyException |
                        BadPaddingException |
                        IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Temporary method to decrypt data previously encrypted by the insecure `encrypt` method.
     * This method uses the old, weak algorithm (AES/ECB/PKCS5PADDING) and hardcoded key.
     * It MUST be removed after all data migration is complete.
     *
     * @param encryptedText The string output from the old `encrypt` method.
     * @return The original plaintext string.
     * @throws java.security.GeneralSecurityException if decryption fails.
     */
    public static String decryptOldData(String encryptedText) throws java.security.GeneralSecurityException {
        // WARNING: This method is for temporary data migration ONLY.
        // It uses the insecure algorithm and hardcoded key from the old implementation.
        // REMOVE this method and all its callers after all data has been successfully migrated
        // to a secure encryption scheme (e.g., AES/GCM with a securely managed key).

        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] encryptedBytes = encryptedText.getBytes(StandardCharsets.UTF_8);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
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
        int secureValue = secureRandom.nextInt(100000) + 1;
        return Integer.toString(secureValue);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weak_cryptography, container, false);
        final EditText secret = view.findViewById(R.id.secret);
        view.findViewById(R.id.encrypt).setOnClickListener(v -> {
            String plain_text = secret.getText().toString();
            if (!plain_text.isEmpty()) {
                try {
                    SecureCryptoManager secureCryptoManager = new SecureCryptoManager();
                    byte[] plaintextBytes = plain_text.getBytes(StandardCharsets.UTF_8);
                    infosecadventures.allsafe.utils.EncryptionResult encryptionResult = secureCryptoManager.encryptData(plaintextBytes, secureCryptoManager.getOrCreateSecretKey());
                    String ciphertextEncoded = Base64.encodeToString(encryptionResult.getCiphertext(), Base64.DEFAULT);
                    SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Result: " + ciphertextEncoded);
                } catch (Exception e) {
                    Log.e("WeakCryptography", "Encryption failed", e);
                    SnackUtil.INSTANCE.simpleMessage(requireActivity(), "Encryption failed: " + e.getMessage());
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