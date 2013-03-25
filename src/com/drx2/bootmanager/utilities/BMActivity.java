package com.drx2.bootmanager.utilities;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;

public class BMActivity extends Activity {

	private static String tmpA;

	private static String decrypt(String seed, String encrypted) throws Exception {  
        byte[] rawKey = getRawKey(seed.getBytes());  
        byte[] enc = toByte(encrypted);  
        byte[] result = decrypt(rawKey, enc);  
        return new String(result);  
    }  
 
    private static byte[] getRawKey(byte[] seed) throws Exception {  
        KeyGenerator kgen = KeyGenerator.getInstance("AES");  
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");  
        sr.setSeed(seed);  
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();  
        byte[] raw = skey.getEncoded();  
        return raw;  
    }  

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {  
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");  
        Cipher cipher = Cipher.getInstance("AES");  
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);  
        byte[] decrypted = cipher.doFinal(encrypted);  
        return decrypted;  
    }  
 
    private static byte[] toByte(String hexString) {  
        int len = hexString.length()/2;  
        byte[] result = new byte[len];  
        for (int i = 0; i < len; i++)  
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();  
        return result;  
    }
    
    public static String DEFAULT_PACKAGE = "ytisrevinufoerawaled";
    public static String edocedlru(String s) {
    	try {
    		tmpA = decrypt(DEFAULT_PACKAGE, s);
    	} catch (Exception e) {
    		
    	}
    	return tmpA;
    }
}