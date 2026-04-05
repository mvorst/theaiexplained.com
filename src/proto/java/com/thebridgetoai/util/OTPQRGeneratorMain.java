package com.thebridgetoai.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.mattvorst.shared.util.Utils;
import org.apache.commons.codec.binary.Base32;

public class OTPQRGeneratorMain {
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

		final TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();

		final Key key;
//		{
//			final KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
//
//			// Key length should match the length of the HMAC output (160 bits for SHA-1, 256 bits
//			// for SHA-256, and 512 bits for SHA-512). Note that while Mac#getMacLength() returns a
//			// length in _bytes,_ KeyGenerator#init(int) takes a key length in _bits._
//			final int macLengthInBytes = Mac.getInstance(totp.getAlgorithm()).getMacLength();
//			keyGenerator.init(macLengthInBytes * 8);
//
//			key = keyGenerator.generateKey();
//		}

		key = new SecretKey() {
			@Override
			public String getAlgorithm() {
				return "HmacSHA1";
			}

			@Override
			public String getFormat() {
				return "RAW";
			}

			@Override
			public byte[] getEncoded() {
				return new Base32().decode("4TI54JBCR3KQI6YRINNOT44UCTJQUZSC");
			}
		};

		Base32 base32 = new Base32();
		String stringKey = base32.encodeAsString(key.getEncoded());

		System.out.println("Key:" + stringKey);
		System.out.println("Format:" + key.getFormat());
		System.out.println("Algorithm:" + key.getAlgorithm());

		final Instant now = Instant.now();
		final Instant later = now.plus(totp.getTimeStep());

		System.out.println("Current password: " + totp.generateOneTimePasswordString(key, now));
		System.out.println("Future password:  " + totp.generateOneTimePasswordString(key, later));

		System.out.println("Complete");
		System.exit(0);
	}
}
