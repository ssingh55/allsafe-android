package infosecadventures.allsafe.challenges;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import infosecadventures.allsafe.R;
import infosecadventures.allsafe.utils.SnackUtil;

public class WeakCryptography extends Fragment {

    public static final String KEY = "1nf053c4dv3n7ur3";

    public static String encrypt(String value) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
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
        SecureRandom rnd = new SecureRandom();
        int n = rnd.nextInt(100000) + 1;
        return Integer.toString(n);
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