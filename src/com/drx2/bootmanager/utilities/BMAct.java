package com.drx2.bootmanager.utilities;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.app.Activity;

public class BMAct extends Activity {

	private static String tmpB;

    private static String encrypt(String seed, String cleartext) throws Exception {  
        byte[] rawKey = getRawKey(seed.getBytes());  
        byte[] result = encrypt(rawKey, cleartext.getBytes());  
        return toHex(result);  
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
 
     
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {  
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");  
        Cipher cipher = Cipher.getInstance("AES");  
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);  
        byte[] encrypted = cipher.doFinal(clear);  
        return encrypted;  
    }  

    private static String toHex(byte[] buf) {  
        if (buf == null)  
            return "";  
        StringBuffer result = new StringBuffer(2*buf.length);  
        for (int i = 0; i < buf.length; i++) {  
            appendHex(result, buf[i]);  
        }  
        return result.toString();  
    }
    
    private final static String HEX = "0123456789ABCDEF";  
    private static void appendHex(StringBuffer sb, byte b) {  
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));  
    }  
    
    public static String DEFAULT_PACKAGE = "ytisrevinufoerawaled";
    public static String edocnelru(String s) {
    	try {
    		tmpB = encrypt(DEFAULT_PACKAGE, s);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return tmpB;
    }
}