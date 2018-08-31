package com.abadie.moran.fivewords;

import android.util.Log;

import java.math.BigInteger;
import java.util.Random;

public class RSA {

    public static String crypt(String line, String public_0, String public_1) {

        BigInteger[] keys = calculateKeys();
        keys[0] = new BigInteger(public_0);
        keys[2] = new BigInteger(public_1);
        Log.d("keys[0]", keys[0].toString());
        Log.d("keys[2]", keys[2].toString());
        // Uses the calculateKeys method to generate key pairs
        BigInteger[] cypher = encrypt(line, keys);
        // The encrypt method encrypts the message and stores it as cypher
        // The decrypt method decrypts the encrypted message
        String str = "";
        for (BigInteger b: cypher) {
            str += b.toString() + ";";

        }
        return str;
    }

    private static BigInteger[] encrypt(String message, BigInteger[] keys) {
        int[] chars = strNum(message);
        BigInteger[] cypher = new BigInteger[chars.length];
        for (int i = 0; i < chars.length; i++) {
            cypher[i] = BigInteger.valueOf(chars[i]).modPow(keys[0], keys[2]);
        }
        return cypher;
    }

    private static String decrypt(BigInteger[] cypher, BigInteger[] keys) {
        int[] chars = new int[cypher.length];
        for (int i = 0; i < cypher.length; i++) {
            chars[i] = cypher[i].modPow(keys[1], keys[2]).intValue();
        }
        return numStr(chars);
    }

    private static BigInteger[] calculateKeys(){
        BigInteger p = primeGen();
        BigInteger q = primeGen();
        BigInteger e = BigInteger.valueOf(13);
        // Totient = (p - 1) * (q - 1)
        BigInteger totient = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        while (totient.mod(e).equals(BigInteger.ZERO)) {
            p = primeGen();
            q = primeGen();
            totient = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        }
        BigInteger d = mulInverse(e, totient);
        BigInteger n = p.multiply(q);
        BigInteger[] keys = new BigInteger[3];
        keys[0] = e; keys[1] = d; keys[2] = n;
        return keys;
    }

    private static BigInteger primeGen() {
        //Generate Random Prime Numbers up to big length 12
        Random rand = new Random();
        BigInteger n = new BigInteger(12, rand);
        while (! isPrime(n)) {
            n = n.add(BigInteger.ONE);
        }
        return n;
    }

    public static boolean isPrime(BigInteger n) {
        //Probabilistic check of the primality of an input number
        //Uses the Miller-Rabin Primality Test
        //http://rosettacode.org/wiki/Miller-Rabin_primality_test
        if (n.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            return false;
        }

        BigInteger s = n.subtract(BigInteger.ONE);
        BigInteger t = BigInteger.ZERO;
        while (s.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            s = s.shiftRight(1);
            t = t.add(BigInteger.ONE);
        }

        int[] testValues = new int[]{7, 11, 13, 17};

        for (int a : testValues) {
            if (try_composite(a, s, n, t)) {
                return false;
            }
        }
        return true;
    }

    private static boolean try_composite(int a, BigInteger s, BigInteger n, BigInteger t) {
        BigInteger aB = BigInteger.valueOf(a);
        if (aB.modPow(s, n).equals(BigInteger.ONE)) {
            return false;
        }
        for (int i = 0; BigInteger.valueOf(i).compareTo(s) < 0; i++) {
            // Python: if pow(a, 2**i * s, n) == n-1
            if (aB.modPow(BigInteger.valueOf(2).pow(i).multiply(s), n).equals(n.subtract(BigInteger.ONE))) {
                return false;
            }
        }
        return true;
    }

    public static BigInteger mulInverse(BigInteger a, BigInteger m) {
        //Calculate Multiplicative Inverse of a wrt m
        BigInteger d = extendedEuclid(a, m)[1].mod(m);
        if (d.compareTo(BigInteger.ZERO) == -1) {
            return d.add(m);
        }
        return d;
    }

    public static BigInteger[] extendedEuclid(BigInteger a, BigInteger b) {
        //Extended Euclidean Algorithm to calculate GDC and Bezout's Identity
        if (a.equals(BigInteger.ZERO)) {
            BigInteger[] gdc = new BigInteger[3];
            gdc[0] = b; gdc[1] = BigInteger.ZERO; gdc[2] = BigInteger.ONE;
            return gdc;
        }
        BigInteger[] x = extendedEuclid(b.mod(a), a);
        BigInteger[] gdc = new BigInteger[3];
        gdc[0] = a; gdc[1]  = x[2].subtract(b.divide(a).multiply(x[1])); gdc[2] = x[1];
        return gdc;
    }


    private static int[] strNum(String str) {
        //Convert an input string into and intArray
        int[] characters = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            characters[i] = (int) str.charAt(i);
        }
        return characters;
    }

    private static String numStr(int[] characters) {
        //Convert from intArray back to String
        String str = "";
        for (int i = 0; i < characters.length; i++) {
            str = str + ((char) characters[i]);
        }
        return str;
    }
}