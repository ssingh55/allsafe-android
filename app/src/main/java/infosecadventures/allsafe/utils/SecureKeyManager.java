package infosecadventures.allsafe.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class SecureKeyManager {
    private static final String KEY_ALIAS = "secure_app_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    public static SecretKey getOrCreateSecretKey() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build());
            return keyGenerator.generateKey();
        }
        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }
}
