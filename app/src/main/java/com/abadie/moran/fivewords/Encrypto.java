package com.abadie.moran.fivewords;
import android.util.Base64;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class Encrypto {
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA/None/PKCS1PADDING");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        Log.d("dsfdfdfdffdf", new String(pair.getPublic().getEncoded(), "UTF-8"));
        return pair;
    }



    public static String encrypt(String publicKey, String plainText) throws Exception {
        generateKeyPair();
        byte[] publicBytes = publicKey.getBytes("UTF-8");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));

        return new String(cipherText, "UTF-8");
    }





}