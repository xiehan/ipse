package ipse.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class PasswordService
{
	public static synchronized String encrypt(String plaintext)
	{
		MessageDigest mdEnc;
		try {
			mdEnc = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} // Encryption algorithm
		mdEnc.update(plaintext.getBytes(), 0, plaintext.length());
		String hash = new BigInteger(1, mdEnc.digest()).toString(16);
		return hash; //step 6
	}
}
