package com.mattvorst.shared.util;

/*
 * Copyright (c) 2025 Matt Vorst
 * All Rights Reserved.
 * Using, copying, modifying, or distributing this software is strictly prohibited without the explicit written consent of Matt Vorst.
 */


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

public class Encryption
{
	public static final char[] CHARS = new char[]{'A','B','C','D','E','F','G','H','I','J','K','M','N','O','P','R','S','T','U','V','W','X','Y','Z','2','3','4','5','6','7','8','9'};

	public static final int AES_KEY_SIZE = 256;
	public static final int GCM_IV_LENGTH = 12;
	public static final int GCM_TAG_LENGTH = 16;

	//	I - L, 1
	//	O - Q, 0

	public static byte[] md5Hash(byte[] data)throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");

		md.reset();

		md.update(data);

		return md.digest();
	}

	public static String md5HashBase64(String data)throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");

		md.reset();

		md.update(data.getBytes());

		return base64StringFromBytes(md.digest());
	}

	public static String encryptToBase32(String data, byte[] password, byte[] iv) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		return new Base32().encodeAsString(encryptAES128(data.getBytes("UTF-8"), Encryption.md5Hash(password), Encryption.md5Hash(iv)));
	}

	public static String decryptFromBase32(String data, byte[] password, byte[] iv) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		byte[] bytes = new Base32().decode(data);

		return new String(decryptAES128(bytes, Encryption.md5Hash(password), Encryption.md5Hash(iv)));
	}

	public static String encryptToBase64(String data, byte[] password, byte[] iv) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		return Base64.getEncoder().encodeToString(encryptAES128(data.getBytes("UTF-8"), Encryption.md5Hash(password), Encryption.md5Hash(iv)));
	}

	public static String decryptFromBase64(String data, byte[] password, byte[] iv) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		byte[] bytes = Base64.getDecoder().decode(data);

		return new String(decryptAES128(bytes, Encryption.md5Hash(password), Encryption.md5Hash(iv)));
	}

	public static byte[] encryptAES128(byte[] data, byte[] password, byte[] iv) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		// Create a key from the supplied passphrase.
		SecretKeySpec keySpec = new SecretKeySpec(password, "AES");

		// Create the algorithm parameters.
		AlgorithmParameterSpec aps = new IvParameterSpec(iv);

		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, aps);

		return cipher.doFinal(data);
	}

	public static byte[] decryptAES128(byte[] data, byte[] password, byte[] iv) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		// Create a key from the supplied passphrase.
		SecretKeySpec keySpec = new SecretKeySpec(password, "AES");

		// Create the algorithm parameters.
		AlgorithmParameterSpec aps = new IvParameterSpec(iv);

		// Instantiate the cipher
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, aps);

		return cipher.doFinal(data);
	}

	public static byte[] encryptAES256(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

		cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

		// Perform Encryption
		byte[] cipherText = cipher.doFinal(data);

		return cipherText;
	}

	public static byte[] decryptAES256(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

		byte[] decryptedText = cipher.doFinal(data);

		return decryptedText;
	}

	public static String encryptAES256ToBase64(String data, byte[] password, byte[] iv) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		byte[] bytes = encryptAES256(data.getBytes("UTF-8"), Encryption.md5Hash(password), Encryption.md5Hash(iv));
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static String decryptAES256FromBase64(String data, byte[] password, byte[] iv) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		byte[] bytes = Base64.getDecoder().decode(data);
		return new String(decryptAES256(bytes, Encryption.md5Hash(password), Encryption.md5Hash(iv)));
	}

	public static String hexStringFromBytes(byte[] bytes)
	{
		return Hex.encodeHexString(bytes);
	}

	public static byte[] bytesFromHexString(String string) throws DecoderException
	{
		return Hex.decodeHex(string.toCharArray());
	}

	public static String base64StringFromBytes(byte[] bytes)
	{
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] bytesFromBase64String(String string)
	{
		return Base64.getDecoder().decode(string);
	}

	public static String encodeHuman32(int number, int places)
	{
		StringBuilder stringBuilder = new StringBuilder();

		do
		{
			int index = number % CHARS.length;

			stringBuilder.append(CHARS[(int)index]);

			number -= index;
			number /= CHARS.length;
		}while(number != 0);

		while(stringBuilder.length() < places)
		{
			stringBuilder.append(CHARS[0]);
		}

		return stringBuilder.reverse().toString();
	}

	public static int decodeHuman32(String number)
	{
		int count = number.length();
		int multiplier = 1;
		int value = 0;

		number = number.toUpperCase().replaceAll("[L1]", "I").replaceAll("[Q0]", "O");

		for(int x = count - 1 ; x >= 0 ; x--)
		{
			char currentChar = number.charAt(x);
			int position = 0;
			for(char c : CHARS)
			{
				if(c == currentChar)
				{
					break;
				}
				position++;
			}
			value += multiplier * position;
			multiplier *= 32;
		}

		return value;
	}

	/**
	 * Generate a hex encoded HMAC digest using SHA-256 hash algorithm
	 * @param key The key to encrypt the hash with
	 * @param data The data to hash and encrypt
	 * @return A hex encoded signature of the provided data using the provided key
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static String hmac256Signature(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
		String algorithm = "HmacSHA256";
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
		Mac mac = Mac.getInstance(algorithm);
		mac.init(secretKeySpec);

		byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return Hex.encodeHexString(signature);
	}
}